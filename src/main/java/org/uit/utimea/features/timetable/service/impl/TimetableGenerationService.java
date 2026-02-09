package org.uit.utimea.features.timetable.service.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uit.utimea.features.timetable.dto.request.TimetableGenerationRequest;
import org.uit.utimea.shared.entity.*;
import org.uit.utimea.shared.repository.*;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimetableGenerationService {

    private final TimetableInfoRepository timetableInfoRepo;
    private final TimetableDataRepository timetableDataRepo;
    private final TimetableRepository timetableRepo;
    private final SubjectRepository subjectRepo;
    private final RoomRepository roomRepo;
    private final MajorSectionRepository majorSectionRepo;
    private final CodeValueRepository codeValueRepo;

    @Getter @Setter @AllArgsConstructor
    class AlgoTeacher {
        Long id;
        String name;
        Set<Integer> globalBusySlots;
    }

    @Getter @Setter
    class AlgoSubject {
        Long dbId;
        String name;
        AlgoTeacher teacher;
        int specialRoomCount;
        int usedLectures = 0;
        int usedTDA = 0;
        int usedSpecialRooms = 0;

        public AlgoSubject(Long dbId, String name, AlgoTeacher teacher, int specialRoomCount) {
            this.dbId = dbId; this.name = name; this.teacher = teacher; this.specialRoomCount = specialRoomCount;
        }
        public void reset() { usedLectures = 0; usedTDA = 0; usedSpecialRooms = 0; }

        public int getPriorityScore() {
            int score = 0;
            if (specialRoomCount == 4) score += 100;
            if (specialRoomCount > 0) score += 50;
            return score;
        }
    }

    @Getter @Setter
    class AlgoRoom {
        Long dbId;
        String name;
        boolean isLab;
        Set<Integer> globalBusySlots = new HashSet<>();
        public AlgoRoom(Long dbId, String name, boolean isLab) {
            this.dbId = dbId; this.name = name; this.isLab = isLab;
        }
    }

    @AllArgsConstructor @Getter
    class ScheduledSlot {
        AlgoSubject subject;
        AlgoRoom room;
        String typeTag;
    }

    // --- MAIN METHOD ---
    @Transactional
    public void generateTimetable(TimetableGenerationRequest request) {

        CodeValue academicYear = codeValueRepo.findById(request.getAcademicYearId())
                .orElseThrow(() -> new RuntimeException("Year not found"));

        // 1. Calculate Number of Sections
        Long studentsCount = request.getNumberOfStudentsInFirstYear();
        int numberOfSections = (int) Math.ceil((double) studentsCount / 40);

        log.info("Generating Timetables for {} Sections ({} Students)...", numberOfSections, studentsCount);

        // 2. Prepare Shared Resources (Teachers & Rooms)
        List<Room> dbRooms = roomRepo.findAll();
        List<Subject> dbSubjects = subjectRepo.findAll();

        Map<Long, AlgoTeacher> globalTeacherMap = new HashMap<>();
        for(Subject s : dbSubjects) {
            Long tId = s.getTeacher().getId();
            globalTeacherMap.putIfAbsent(tId, new AlgoTeacher(tId, s.getTeacher().getName(), new HashSet<>()));
        }

        List<AlgoRoom> algoRooms = mapRooms(dbRooms);

        // 3. Loop Through Each Section (A, B, C...)
        for (int i = 0; i < numberOfSections; i++) {
            char sectionChar = (char) ('A' + i); // 0->A, 1->B...
            String sectionName = "Year 1 - Section " + sectionChar;

            log.info("Processing {}...", sectionName);

            MajorSection majorSection = majorSectionRepo.findByName(sectionName)
                    .orElseThrow(() -> new RuntimeException("Section '" + sectionName + "' not found in DB. Please seed it!"));

            List<AlgoSubject> sectionSubjects = mapSubjectsForSection(dbSubjects, globalTeacherMap);

            // --- RETRY LOGIC ---
            boolean sectionSolved = false;
            int attempts = 0;

            while (!sectionSolved && attempts < 100) {
                attempts++;

                // Reset Subject Counters (Lectures=0) but KEEP Teacher Busy Slots
                for (AlgoSubject s : sectionSubjects) s.reset();

                ScheduledSlot[] rawSchedule = new ScheduledSlot[30];
                Set<Integer> freeSlots = generateBalancedFreeSlots(30);

                if (solve(0, rawSchedule, sectionSubjects, algoRooms, freeSlots)) {
                    log.info("Section {} Solved on attempt #{}", sectionChar, attempts);
                    saveToDatabase(rawSchedule, majorSection, academicYear, freeSlots);
                    sectionSolved = true;
                }
            }

            if (!sectionSolved) {
                throw new RuntimeException("Failed to generate " + sectionName + ". Teachers might be fully booked!");
            }
        }
    }

    // --- SOLVER ---
    private boolean solve(int slot, ScheduledSlot[] schedule, List<AlgoSubject> subjects, List<AlgoRoom> rooms, Set<Integer> freeSlots) {
        if (slot >= 30) return true;
        if (freeSlots.contains(slot)) return solve(slot + 1, schedule, subjects, rooms, freeSlots);

        boolean isMorning = (slot % 6) < 3;

        // SMART SORT: Hardest subjects first
        List<AlgoSubject> sortedSubjects = new ArrayList<>(subjects);
        sortedSubjects.sort((s1, s2) -> s2.getPriorityScore() - s1.getPriorityScore());

        for (AlgoSubject sub : sortedSubjects) {
            if (isMorning && sub.usedLectures >= 2) continue;
            if (!isMorning && sub.usedTDA >= 2) continue;
            if (sub.teacher.globalBusySlots.contains(slot)) continue;
            if (getDailyCount(schedule, slot, sub) >= 2) continue;

            boolean needLab = false;
            if (sub.specialRoomCount == 4) needLab = true;
            else if (sub.specialRoomCount > 0 && sub.usedSpecialRooms < sub.specialRoomCount && !isMorning) needLab = true;

            AlgoRoom assignedRoom = null;
            for (AlgoRoom r : rooms) {
                if (r.isLab() == needLab && !r.globalBusySlots.contains(slot)) {
                    assignedRoom = r;
                    break;
                }
            }
            if (assignedRoom == null) continue;

            sub.teacher.globalBusySlots.add(slot);
            assignedRoom.globalBusySlots.add(slot);
            if(isMorning) sub.usedLectures++; else sub.usedTDA++;
            if(needLab) sub.usedSpecialRooms++;

            schedule[slot] = new ScheduledSlot(sub, assignedRoom, isMorning ? "(L)" : "(TDA)");

            if (solve(slot + 1, schedule, subjects, rooms, freeSlots)) return true;

            schedule[slot] = null;
            sub.teacher.globalBusySlots.remove(slot);
            assignedRoom.globalBusySlots.remove(slot);
            if(isMorning) sub.usedLectures--; else sub.usedTDA--;
            if(needLab) sub.usedSpecialRooms--;
        }
        return false;
    }

    // --- MAPPERS ---
    private List<AlgoSubject> mapSubjectsForSection(List<Subject> dbSubjects, Map<Long, AlgoTeacher> globalTeacherMap) {
        List<AlgoSubject> list = new ArrayList<>();
        for (Subject s : dbSubjects) {
            AlgoTeacher teacher = globalTeacherMap.get(s.getTeacher().getId());

            int roomCount = 0;
            if (s.getRoomType() != null && "Computer Room".equalsIgnoreCase(s.getRoomType().getName())) {
                roomCount = 4;
            } else if (s.getSpecialRoomCount() != null) {
                roomCount = s.getSpecialRoomCount();
            }

            list.add(new AlgoSubject(s.getId(), s.getCode(), teacher, roomCount));
        }
        return list;
    }

    private List<AlgoRoom> mapRooms(List<Room> dbRooms) {
        List<AlgoRoom> list = new ArrayList<>();
        for (Room r : dbRooms) {
            boolean isLab = r.getRoomType() != null && "Computer Room".equalsIgnoreCase(r.getRoomType().getName());
            if (r.getIsSpecialRoom() != null && r.getIsSpecialRoom()) isLab = true;
            list.add(new AlgoRoom(r.getId(), r.getName(), isLab));
        }
        return list;
    }

    private int getDailyCount(ScheduledSlot[] schedule, int currentSlot, AlgoSubject sub) {
        int dayStart = (currentSlot / 6) * 6;
        int count = 0;
        for (int k = dayStart; k < currentSlot; k++) {
            if (schedule[k] != null && schedule[k].getSubject().getDbId().equals(sub.getDbId())) {
                count++;
            }
        }
        return count;
    }

    private Set<Integer> generateBalancedFreeSlots(int totalSlots) {
        Set<Integer> slots = new HashSet<>();
        Random rand = new Random();
        int morningFree = 0; int eveningFree = 0; int maxPerBlock = 3;

        while (slots.size() < 6) {
            int r = rand.nextInt(totalSlots);
            if (slots.contains(r-1) && slots.contains(r-2)) continue;
            if (slots.contains(r+1) && slots.contains(r+2)) continue;
            boolean isMorning = (r % 6) < 3;
            if (isMorning && morningFree >= maxPerBlock) continue;
            if (!isMorning && eveningFree >= maxPerBlock) continue;
            if (slots.add(r)) { if (isMorning) morningFree++; else eveningFree++; }
        }
        return slots;
    }

    private void saveToDatabase(ScheduledSlot[] rawSchedule, MajorSection section, CodeValue year, Set<Integer> freeSlots) {
        TimetableInfo info = new TimetableInfo();
        info.setMajorSection(section);
        info.setAcademicYear(year);
        info.setName(section.getName() + " (" + year.getName() + ")");
        info = timetableInfoRepo.save(info);

        List<CodeValue> allDays = codeValueRepo.findByCode_Name("DAY");
        List<CodeValue> allPeriods = codeValueRepo.findByCode_Name("PERIOD");

        for (int slot = 0; slot < 30; slot++) {
            if (freeSlots.contains(slot) || rawSchedule[slot] == null) continue;

            ScheduledSlot result = rawSchedule[slot];
            TimetableData data = new TimetableData();
            data.setTimetableDay(allDays.get(slot / 6));
            data.setTimetablePeriod(allPeriods.get(slot % 6));
            data.setSubject(subjectRepo.getReferenceById(result.getSubject().getDbId()));
            data.setRoom(roomRepo.getReferenceById(result.getRoom().getDbId()));
            data = timetableDataRepo.save(data);

            Timetable timetable = new Timetable();
            timetable.setTimetableInfo(info);
            timetable.setTimetableData(data);
//            timetable.setName(result.getSubject().getName() + " " + result.getTypeTag());
            timetableRepo.save(timetable);
        }
    }
}