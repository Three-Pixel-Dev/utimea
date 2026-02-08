package org.uit.utimea.features.timetable.service.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uit.utimea.features.timetable.dto.request.TimetableGenerationRequest;
import org.uit.utimea.features.timetable.dto.response.TimetableResponseDto;
import org.uit.utimea.shared.entity.*;
import org.uit.utimea.shared.repository.*;

import java.util.*;
import java.util.stream.Collectors;

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

    @Getter
    @Setter
    @AllArgsConstructor
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

    @Transactional
    public void generateTimetable(TimetableGenerationRequest request) {

        MajorSection majorSection = majorSectionRepo.findById(request.getMajorSectionId())
                .orElseThrow(() -> new RuntimeException("Section not found"));

        CodeValue academicYear = codeValueRepo.findById(request.getAcademicYearId())
                .orElseThrow(() -> new RuntimeException("Year not found"));

        List<Subject> dbSubjects = subjectRepo.findAll();
        List<Room> dbRooms = roomRepo.findAll();

        List<AlgoSubject> algoSubjects = mapSubjects(dbSubjects);
        List<AlgoRoom> algoRooms = mapRooms(dbRooms);

        int SLOTS_PER_WEEK = 30;
        ScheduledSlot[] rawSchedule = new ScheduledSlot[SLOTS_PER_WEEK];

        Set<Integer> freeSlots = generateBalancedFreeSlots(SLOTS_PER_WEEK);

        if (solve(0, rawSchedule, algoSubjects, algoRooms, freeSlots)) {
            log.info("Solution Found! Saving to Database...");
            saveToDatabase(rawSchedule, majorSection, academicYear, freeSlots);
        } else {
            throw new RuntimeException("Could not generate timetable. Constraints too tight.");
        }
    }


    private List<AlgoSubject> mapSubjects(List<Subject> dbSubjects) {
        List<AlgoSubject> list = new ArrayList<>();
        for (Subject s : dbSubjects) {
            AlgoTeacher teacher = new AlgoTeacher(s.getId(), s.getDescription(), new HashSet<>());

            int roomCount = 0;
            if (s.getRoomType() != null && "Computer Room".equalsIgnoreCase(s.getRoomType().getName())) {
                roomCount = 4;
            }

            list.add(new AlgoSubject(s.getId(), s.getCode(), teacher, roomCount));
        }
        return list;
    }

    private List<AlgoRoom> mapRooms(List<Room> dbRooms) {
        List<AlgoRoom> list = new ArrayList<>();
        for (Room r : dbRooms) {
            boolean isLab = r.getRoomType() != null && "Computer Room".equalsIgnoreCase(r.getRoomType().getName());
            list.add(new AlgoRoom(r.getId(), r.getName(), isLab));
        }
        return list;
    }

    @AllArgsConstructor @Getter
    class ScheduledSlot {
        AlgoSubject subject;
        AlgoRoom room;
        String typeTag;
    }

    private boolean solve(int slot, ScheduledSlot[] schedule, List<AlgoSubject> subjects, List<AlgoRoom> rooms, Set<Integer> freeSlots) {
        if (slot >= 30) return true;
        if (freeSlots.contains(slot)) return solve(slot + 1, schedule, subjects, rooms, freeSlots);

        boolean isMorning = (slot % 6) < 3;
        int startIdx = slot % subjects.size();

        for (int i = 0; i < subjects.size(); i++) {
            AlgoSubject sub = subjects.get((startIdx + i) % subjects.size());

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
        int morningFree = 0;
        int eveningFree = 0;

        int maxPerBlock = 3;

        while (slots.size() < 6) {
            int r = rand.nextInt(totalSlots);

            if (slots.contains(r-1) && slots.contains(r-2)) continue;
            if (slots.contains(r+1) && slots.contains(r+2)) continue;

            boolean isMorning = (r % 6) < 3;

            if (isMorning && morningFree >= maxPerBlock) continue;
            if (!isMorning && eveningFree >= maxPerBlock) continue;

            if (slots.add(r)) {
                if (isMorning) morningFree++; else eveningFree++;
            }
        }
        return slots;
    }


    private void saveToDatabase(ScheduledSlot[] rawSchedule, MajorSection section, CodeValue year, Set<Integer> freeSlots) {
        TimetableInfo info = new TimetableInfo();
        info.setMajorSection(section);
        info.setAcademicYear(year);
        info = timetableInfoRepo.save(info);

        List<CodeValue> allDays = codeValueRepo.findByCode_Name("DAY");
        List<CodeValue> allPeriods = codeValueRepo.findByCode_Name("PERIOD");

        for (int slot = 0; slot < 30; slot++) {
            if (freeSlots.contains(slot) || rawSchedule[slot] == null) continue;

            ScheduledSlot result = rawSchedule[slot];

            TimetableData data = new TimetableData();

            int dayIndex = slot / 6;
            int periodIndex = slot % 6;

            data.setTimetableDay(allDays.get(dayIndex));
            data.setTimetablePeriod(allPeriods.get(periodIndex));

            data.setSubject(subjectRepo.getReferenceById(result.getSubject().getDbId()));
            data.setRoom(roomRepo.getReferenceById(result.getRoom().getDbId()));

            data = timetableDataRepo.save(data);

            Timetable timetable = new Timetable();
            timetable.setTimetableInfo(info);
            timetable.setTimetableData(data);
            timetableRepo.save(timetable);
        }
    }


//    public List<TimetableResponseDto> getTimetableBySection(Long majorSectionId) {
//        TimetableInfo info = timetableInfoRepo.findByMajorSection_Id(majorSectionId)
//                .stream().findFirst()
//                .orElseThrow(() -> new RuntimeException("No timetable found for this section"));
//
//        List<Timetable> entries = timetableRepo.findByTimetableInfo_Id(info.getId());
//
//        return entries.stream()
//                .map(t -> {
//                    TimetableData data = t.getTimetableData();
//                    return TimetableResponseDto.builder()
//                            .id(t.getId())
//                            .day(data.getTimetableDay().getName())
//                            .period(data.getTimetablePeriod().getName())
//                            .subject(data.getSubject().getDescription())
//                            .room(data.getRoom().getName())
//                            .label(t.getName())
//                            .build();
//                })
//                .sorted(Comparator.comparing((TimetableResponseDto d) -> d.getDay())
//                        .thenComparing(d -> d.getPeriod()))
//                .collect(Collectors.toList());
//    }
}
