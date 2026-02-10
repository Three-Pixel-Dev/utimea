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
    private final ProfileRepository profileRepo;

    // --- INNER HELPER CLASSES ---

    @Getter @Setter
    class AlgoTeacher {
        Long id;
        String name;
        Set<Integer> globalBusySlots;

        public AlgoTeacher(Long id, String name, Set<Integer> globalBusySlots) {
            this.id = id;
            this.name = name;
            this.globalBusySlots = globalBusySlots;
        }
    }

    @Getter @Setter
    class AlgoSubject {
        Long dbId;
        String code;
        String name;
        List<AlgoTeacher> teachers;
        int specialRoomCount;
        boolean requiresComputerRoom;

        // Counters for current recursion
        int usedLectures = 0;
        int usedTDA = 0;
        int usedSpecialRooms = 0;

        public AlgoSubject(Long dbId, String code, String name, List<AlgoTeacher> teachers, int specialRoomCount, boolean requiresComputerRoom) {
            this.dbId = dbId;
            this.code = code;
            this.name = name;
            this.teachers = teachers;
            this.specialRoomCount = specialRoomCount;
            this.requiresComputerRoom = requiresComputerRoom;
        }

        public void reset() { usedLectures = 0; usedTDA = 0; usedSpecialRooms = 0; }

        public int getPriorityScore() {
            int score = 0;
            if (requiresComputerRoom) score += 200;
            if (specialRoomCount > 0) score += 50;
            return score;
        }
    }

    @Getter @Setter
    class AlgoRoom {
        Long dbId;
        String name;
        boolean isComputerRoom;
        boolean isSpecialRoom;
        Set<Integer> globalBusySlots = new HashSet<>();

        public AlgoRoom(Long dbId, String name, boolean isComputerRoom, boolean isSpecialRoom) {
            this.dbId = dbId;
            this.name = name;
            this.isComputerRoom = isComputerRoom;
            this.isSpecialRoom = isSpecialRoom;
        }
    }

    @AllArgsConstructor @Getter
    class ScheduledSlot {
        AlgoSubject subject;
        AlgoRoom room;
        AlgoTeacher assignedTeacher;
        String typeTag;
    }

    // --- MAIN ENTRY POINT ---

    @Transactional
    public void generateTimetable(TimetableGenerationRequest request) {
        long startTime = System.currentTimeMillis();

        CodeValue academicYear = codeValueRepo.findById(request.getAcademicYearId())
                .orElseThrow(() -> new RuntimeException("Academic Year not found with ID: " + request.getAcademicYearId()));

        boolean isFirstSem = "FIRST_SEM".equalsIgnoreCase(request.getSem());
        log.info(">>> STARTING GENERATION: Year ID={}, Sem={}", request.getAcademicYearId(), request.getSem());

        // 1. Load Global Resources
        List<Room> dbRooms = roomRepo.findAll();
        List<Subject> dbSubjects = subjectRepo.findAll();

        // 2. RUN VALIDATION
        validateFeasibility(request, dbSubjects, dbRooms, isFirstSem);

        // 3. Map Resources
        Map<Long, AlgoTeacher> globalTeacherMap = new HashMap<>();
        for(Subject s : dbSubjects) {
            for(Profile t : s.getTeachers()) {
                globalTeacherMap.putIfAbsent(t.getId(), new AlgoTeacher(t.getId(), t.getName(), new HashSet<>()));
            }
        }
        List<AlgoRoom> algoRooms = mapRooms(dbRooms);

        // 4. Generate Schedules

        // Year 1 & 2
        if (hasStudents(request.getNumberOfStudentsInFirstYear())) {
            generateSectionBasedSchedule("FIRST_YEAR", request.getNumberOfStudentsInFirstYear(), isFirstSem,
                    dbSubjects, globalTeacherMap, algoRooms, academicYear);
        }
        if (hasStudents(request.getNumberOfStudentsInSecondYear())) {
            generateSectionBasedSchedule("SECOND_YEAR", request.getNumberOfStudentsInSecondYear(), isFirstSem,
                    dbSubjects, globalTeacherMap, algoRooms, academicYear);
        }

        // Major List
        List<String> majors = Arrays.asList("SE", "KE", "HPC", "BIS", "CN", "ES", "CSec");

        if (isFirstSem) {
            if (hasStudents(request.getNumberOfStudentInThirdYear())) {
                generateMajorBasedSchedule("THIRD_YEAR", 0L, true, dbSubjects, globalTeacherMap, algoRooms, academicYear, majors);
            }
            generateMajorBasedSchedule("FOURTH_YEAR", 0L, true, dbSubjects, globalTeacherMap, algoRooms, academicYear, majors);
        } else {
            // Second Sem
            if (hasStudents(request.getNumberOfStudentInThirdYear())) {
                Map<String, Integer> y3Counts = request.getThirdYearMajorCounts();
                if (y3Counts == null || y3Counts.isEmpty()) y3Counts = createDefaultCounts(majors);
                generateSem2CombinedSchedule("THIRD_YEAR", y3Counts, dbSubjects, globalTeacherMap, algoRooms, academicYear);
            }

            Map<String, Integer> y4Counts = request.getFourthYearMajorCounts();
            if (y4Counts == null || y4Counts.isEmpty()) y4Counts = createDefaultCounts(majors);
            generateSem2CombinedSchedule("FOURTH_YEAR", y4Counts, dbSubjects, globalTeacherMap, algoRooms, academicYear);
        }

        long totalTime = System.currentTimeMillis() - startTime;
        log.info(">>> GENERATION COMPLETE. Total time: {}s", totalTime / 1000.0);
    }

    private boolean hasStudents(Long count) {
        return count != null && count > 0;
    }

    private Map<String, Integer> createDefaultCounts(List<String> majors) {
        Map<String, Integer> counts = new HashMap<>();
        for (String m : majors) counts.put(m, 20);
        return counts;
    }

    // --- STRATEGY 3: SEMESTER 2 ---
    private void generateSem2CombinedSchedule(String yearCode, Map<String, Integer> majorCounts,
                                              List<Subject> allSubjects, Map<Long, AlgoTeacher> globalTeacherMap,
                                              List<AlgoRoom> algoRooms, CodeValue academicYear) {
        String yearLabel = switch (yearCode) {
            case "THIRD_YEAR" -> "Year 3";
            case "FOURTH_YEAR" -> "Year 4";
            default -> yearCode;
        };
        log.info("Generating Combined Schedule for {} (Sem 2)...", yearLabel);

        List<Subject> semesterSubjects = allSubjects.stream()
                .filter(s -> s.getSubjectYear().name().equalsIgnoreCase(yearCode))
                .filter(s -> s.getIsFirstSem() != null && !s.getIsFirstSem())
                .collect(Collectors.toList());

        Map<String, List<String>> subjectToMajors = new HashMap<>();
        for (Subject s : semesterSubjects) {
            if (s.getCode().startsWith("CST-") || s.getCode().startsWith("CS-")) {
                subjectToMajors.put(s.getCode(), new ArrayList<>(majorCounts.keySet()));
            } else {
                String prefix = s.getCode().split("-")[0];
                if (majorCounts.containsKey(prefix)) {
                    subjectToMajors.put(s.getCode(), Collections.singletonList(prefix));
                } else {
                    subjectToMajors.put(s.getCode(), new ArrayList<>(majorCounts.keySet()));
                }
            }
        }

        Map<String, ScheduledSlot[]> majorTimetables = new HashMap<>();
        for (String major : majorCounts.keySet()) {
            majorTimetables.put(major, new ScheduledSlot[30]);
        }

        for (Subject sub : semesterSubjects) {
            List<String> takingMajors = subjectToMajors.getOrDefault(sub.getCode(), Collections.emptyList());
            if (takingMajors.isEmpty()) continue;

            AlgoSubject algoSub = mapToAlgoSubject(sub, globalTeacherMap);

            boolean scheduled = false;
            for (int slot = 0; slot < 30; slot++) {
                if (scheduled) break;

                boolean slotFreeForEveryone = true;
                for (String major : takingMajors) {
                    if (majorTimetables.get(major)[slot] != null) {
                        slotFreeForEveryone = false;
                        break;
                    }
                }

                if (slotFreeForEveryone) {
                    if (tryBookSlot(slot, algoSub, algoRooms)) {
                        ScheduledSlot result = new ScheduledSlot(algoSub, getAssignedRoom(slot, algoSub, algoRooms), getAssignedTeacher(slot, algoSub), "(Combined)");

                        for (String major : takingMajors) {
                            majorTimetables.get(major)[slot] = result;
                        }
                        scheduled = true;
                    }
                }
            }
        }

        for (String major : majorCounts.keySet()) {
            String sectionName = String.format("%s (%s) - Section A", yearLabel, major);
            saveToDatabaseWithArray(majorTimetables.get(major), sectionName, academicYear);
        }
    }

    private boolean tryBookSlot(int slot, AlgoSubject sub, List<AlgoRoom> rooms) {
        for (AlgoTeacher t : sub.teachers) {
            if (!t.globalBusySlots.contains(slot)) {
                for (AlgoRoom r : rooms) {
                    if (!r.globalBusySlots.contains(slot)) {
                        t.globalBusySlots.add(slot);
                        r.globalBusySlots.add(slot);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private AlgoRoom getAssignedRoom(int slot, AlgoSubject sub, List<AlgoRoom> rooms) {
        return rooms.stream().filter(r -> r.globalBusySlots.contains(slot)).findFirst().orElse(null);
    }

    private AlgoTeacher getAssignedTeacher(int slot, AlgoSubject sub) {
        return sub.teachers.stream().filter(t -> t.globalBusySlots.contains(slot)).findFirst().orElse(null);
    }

    private void saveToDatabaseWithArray(ScheduledSlot[] rawSchedule, String sectionName, CodeValue year) {
        Optional<MajorSection> sectionOpt = majorSectionRepo.findByName(sectionName);
        if (sectionOpt.isEmpty()) return;
        saveToDatabase(rawSchedule, sectionOpt.get(), year, new HashSet<>());
    }

    // --- RECURSIVE SOLVER ---
    private boolean solve(int slot, ScheduledSlot[] schedule, List<AlgoSubject> subjects, List<AlgoRoom> rooms, Set<Integer> freeSlots) {
        if (slot >= 30) return true;
        if (freeSlots.contains(slot)) return solve(slot + 1, schedule, subjects, rooms, freeSlots);

        boolean isMorning = (slot % 6) < 3;

        List<AlgoSubject> sortedSubjects = new ArrayList<>(subjects);
        sortedSubjects.sort((s1, s2) -> s2.getPriorityScore() - s1.getPriorityScore());

        for (AlgoSubject sub : sortedSubjects) {
            if (isMorning && sub.usedLectures >= 2) continue;
            if (!isMorning && sub.usedTDA >= 2) continue;
            if (getDailyCount(schedule, slot, sub) >= 2) continue;

            AlgoTeacher assignedTeacher = null;
            int minBusy = Integer.MAX_VALUE;

            for (AlgoTeacher t : sub.teachers) {
                if (!t.globalBusySlots.contains(slot)) {
                    if (t.globalBusySlots.size() < minBusy) {
                        minBusy = t.globalBusySlots.size();
                        assignedTeacher = t;
                    }
                }
            }
            if (assignedTeacher == null) continue;

            boolean lookingForComputerRoom = sub.requiresComputerRoom;
            boolean lookingForSpecialRoom = false;

            if (!lookingForComputerRoom && sub.specialRoomCount > 0 && !isMorning && sub.usedSpecialRooms < sub.specialRoomCount) {
                lookingForSpecialRoom = true;
            }

            AlgoRoom assignedRoom = null;
            for (AlgoRoom r : rooms) {
                if (r.globalBusySlots.contains(slot)) continue;

                if (lookingForComputerRoom) {
                    if (r.isComputerRoom) { assignedRoom = r; break; }
                }
                else if (lookingForSpecialRoom) {
                    if (r.isSpecialRoom) { assignedRoom = r; break; }
                }
                else {
                    if (!r.isComputerRoom && !r.isSpecialRoom) { assignedRoom = r; break; }
                }
            }

            if (assignedRoom == null) continue;

            assignedTeacher.globalBusySlots.add(slot);
            assignedRoom.globalBusySlots.add(slot);

            if(isMorning) sub.usedLectures++; else sub.usedTDA++;
            if(lookingForComputerRoom || lookingForSpecialRoom) sub.usedSpecialRooms++;

            schedule[slot] = new ScheduledSlot(sub, assignedRoom, assignedTeacher, isMorning ? "(L)" : "(TDA)");

            if (solve(slot + 1, schedule, subjects, rooms, freeSlots)) return true;

            // Backtrack
            schedule[slot] = null;
            assignedTeacher.globalBusySlots.remove(slot);
            assignedRoom.globalBusySlots.remove(slot);

            if(isMorning) sub.usedLectures--; else sub.usedTDA--;
            if(lookingForComputerRoom || lookingForSpecialRoom) sub.usedSpecialRooms--;
        }

        // --- STRICT SLOT FILLING---
        // Only allow empty slot if ALL subjects have met their quota (4 hours).
        boolean allSubjectsCompleted = subjects.stream()
                .allMatch(s -> (s.usedLectures + s.usedTDA) >= 4);

        if (allSubjectsCompleted) {
            schedule[slot] = null;
            if (solve(slot + 1, schedule, subjects, rooms, freeSlots)) return true;
        }

        return false;
    }

    // --- MAPPERS ---
    private List<AlgoSubject> filterAndMapSubjects(List<Subject> allSubjects, String yearCode, boolean isFirstSem, Map<Long, AlgoTeacher> globalTeacherMap) {
        return allSubjects.stream()
                .filter(s -> s.getSubjectYear().name().equalsIgnoreCase(yearCode))
                .filter(s -> s.getIsFirstSem() == isFirstSem)
                .map(s -> mapToAlgoSubject(s, globalTeacherMap))
                .collect(Collectors.toList());
    }

    private AlgoSubject mapToAlgoSubject(Subject s, Map<Long, AlgoTeacher> globalTeacherMap) {
        List<AlgoTeacher> subjectTeachers = new ArrayList<>();
        for(Profile p : s.getTeachers()) {
            AlgoTeacher algoT = globalTeacherMap.get(p.getId());
            if(algoT != null) subjectTeachers.add(algoT);
        }
        Collections.shuffle(subjectTeachers);

        boolean requiresComputerRoom = false;
        int spCount = (s.getSpecialRoomCount() != null) ? s.getSpecialRoomCount() : 0;

        if (s.getRoomType() != null) {
            String typeName = s.getRoomType().getName();
            if ("Computer Room".equalsIgnoreCase(typeName) || "PC".equalsIgnoreCase(typeName)) {
                requiresComputerRoom = true;
                spCount = 4;
            }
        }

        return new AlgoSubject(s.getId(), s.getCode(), s.getCode(), subjectTeachers, spCount, requiresComputerRoom);
    }

    private List<AlgoRoom> mapRooms(List<Room> dbRooms) {
        List<AlgoRoom> list = new ArrayList<>();
        for (Room r : dbRooms) {
            boolean isComp = isComputerRoom(r);
            boolean isSpecial = isSpecialRoom(r);
            list.add(new AlgoRoom(r.getId(), r.getName(), isComp, isSpecial));
        }
        return list;
    }

    // --- VALIDATION LOGIC ---
    private void validateFeasibility(TimetableGenerationRequest request, List<Subject> allSubjects, List<Room> allRooms, boolean isFirstSem) {
        log.info("--- Running Pre-flight Validation ---");

        if (allRooms.isEmpty()) {
            throw new RuntimeException("Validation Failed: No rooms found in the database.");
        }

        long normalRoomSlots = allRooms.stream().filter(r -> !isComputerRoom(r) && !isSpecialRoom(r)).count() * 30;
        long compRoomSlots = allRooms.stream().filter(this::isComputerRoom).count() * 30;
        long specialRoomSlots = allRooms.stream().filter(r -> isSpecialRoom(r) && !isComputerRoom(r)).count() * 30;
        long hybridSlots = allRooms.stream().filter(r -> isSpecialRoom(r) && isComputerRoom(r)).count() * 30;

        log.info("Room Supply: Normal={} slots, Comp={} slots, Pure Special={} slots, Hybrid(Comp+Special)={} slots",
                normalRoomSlots, compRoomSlots, specialRoomSlots, hybridSlots);

        long totalSpecialCapable = specialRoomSlots + hybridSlots;

        Map<String, Integer> validationMultipliers = new HashMap<>();

        if (hasStudents(request.getNumberOfStudentsInFirstYear()))
            validationMultipliers.put("FIRST_YEAR", (int) Math.ceil((double) request.getNumberOfStudentsInFirstYear() / 40));
        if (hasStudents(request.getNumberOfStudentsInSecondYear()))
            validationMultipliers.put("SECOND_YEAR", (int) Math.ceil((double) request.getNumberOfStudentsInSecondYear() / 40));

        validationMultipliers.put("THIRD_YEAR", 1);
        validationMultipliers.put("FOURTH_YEAR", 1);

        Demand totalDemand = new Demand(0, 0, 0);
        Map<String, Integer> subjectDemand = new HashMap<>();
        Map<String, Set<Long>> subjectTeachers = new HashMap<>();

        for (Map.Entry<String, Integer> entry : validationMultipliers.entrySet()) {
            String yearCode = entry.getKey();
            int multiplier = entry.getValue();

            List<Subject> yearSubjects = allSubjects.stream()
                    .filter(sub -> sub.getSubjectYear().name().equalsIgnoreCase(yearCode))
                    .filter(sub -> sub.getIsFirstSem() == isFirstSem)
                    .toList();

            Demand d = calculateDemandForSubjects(yearCode, yearSubjects, multiplier);
            totalDemand = new Demand(totalDemand.normal + d.normal, totalDemand.comp + d.comp, totalDemand.special + d.special);

            for (Subject s : yearSubjects) {
                int needed = 4 * multiplier;
                subjectDemand.merge(s.getCode(), needed, Integer::sum);
                Set<Long> tIds = s.getTeachers().stream().map(Profile::getId).collect(Collectors.toSet());
                if (tIds.isEmpty()) throw new RuntimeException("Data Error: Subject " + s.getCode() + " has no teachers!");
                subjectTeachers.put(s.getCode(), tIds);
            }
        }

        log.info("Room Demand: Normal={} slots, Comp={} slots, Special={} slots", totalDemand.normal, totalDemand.comp, totalDemand.special);

        if (totalDemand.normal > normalRoomSlots)
            log.warn("POTENTIAL ISSUE: Require {} Normal slots, but only {} available. Will try to proceed.", totalDemand.normal, normalRoomSlots);

        if (totalDemand.comp > compRoomSlots)
            log.warn("POTENTIAL ISSUE: Require {} Comp slots, but only {} available. Will try to proceed.", totalDemand.comp, compRoomSlots);

        if (totalDemand.special > totalSpecialCapable)
            log.warn("POTENTIAL ISSUE: Require {} Special slots, but only {} available (including hybrids). Will try to proceed.", totalDemand.special, totalSpecialCapable);

        Map<Long, Integer> estimatedTeacherLoad = new HashMap<>();
        for (Map.Entry<String, Integer> entry : subjectDemand.entrySet()) {
            int hours = entry.getValue();
            Set<Long> tIds = subjectTeachers.get(entry.getKey());
            int splitLoad = (int) Math.ceil((double) hours / tIds.size());
            for (Long tId : tIds) {
                estimatedTeacherLoad.merge(tId, splitLoad, Integer::sum);
            }
        }

        for (Map.Entry<Long, Integer> entry : estimatedTeacherLoad.entrySet()) {
            if (entry.getValue() > 32) {
                String tName = allSubjects.stream().flatMap(s -> s.getTeachers().stream()).filter(p -> p.getId().equals(entry.getKey())).findFirst().map(Profile::getName).orElse("Unknown");
                log.error("GENERATION RISK: Teacher {} (ID: {}) requires ~{} hours/week (Max 30).", tName, entry.getKey(), entry.getValue());
            }
        }
        log.info("Validation Finished (Warnings Logged). Proceeding...");
    }

    record Demand(long normal, long comp, long special) {}

    private Demand calculateDemandForSubjects(String yearLabel, List<Subject> subjects, int sections) {
        long n = 0, c = 0, s = 0;
        if (subjects.isEmpty()) return new Demand(0,0,0);
        for (Subject sub : subjects) {
            boolean isComp = isComputerRoom(sub);
            int specialCount = sub.getSpecialRoomCount() != null ? sub.getSpecialRoomCount() : 0;
            if (isComp) {
                c += (4L * sections);
            } else {
                long specialNeeds = (long) specialCount * sections;
                long normalNeeds = (4L - specialCount) * sections;
                if (normalNeeds < 0) normalNeeds = 0;
                s += specialNeeds;
                n += normalNeeds;
            }
        }
        log.info("   > {}: {} Sections (Multiplier) -> Normal={}, Comp={}, Special={}", yearLabel, sections, n, c, s);
        return new Demand(n, c, s);
    }

    // --- REUSED HELPERS ---
    private boolean isComputerRoom(Room r) {
        if (r.getRoomType() == null) return false;
        String name = r.getRoomType().getName();
        return "Computer Room".equalsIgnoreCase(name) || "PC".equalsIgnoreCase(name);
    }
    private boolean isComputerRoom(Subject s) {
        if (s.getRoomType() == null) return false;
        String name = s.getRoomType().getName();
        return "Computer Room".equalsIgnoreCase(name) || "PC".equalsIgnoreCase(name);
    }
    private boolean isSpecialRoom(Room r) { return r.getIsSpecialRoom() != null && r.getIsSpecialRoom(); }

    private void generateSectionBasedSchedule(String yearCode, Long studentCount, boolean isFirstSem, List<Subject> allSubjects, Map<Long, AlgoTeacher> globalTeacherMap, List<AlgoRoom> algoRooms, CodeValue academicYear) {
        int numberOfSections = (int) Math.ceil((double) studentCount / 40);
        String yearLabel = yearCode.equals("FIRST_YEAR") ? "Year 1" : "Year 2";
        List<AlgoSubject> yearSubjects = filterAndMapSubjects(allSubjects, yearCode, isFirstSem, globalTeacherMap);
        for (int i = 0; i < numberOfSections; i++) {
            char sectionChar = (char) ('A' + i);
            processSection(yearLabel + " - Section " + sectionChar, yearSubjects, algoRooms, academicYear);
        }
    }

    private void generateMajorBasedSchedule(String yearCode, Long totalStudentCount, boolean isFirstSem, List<Subject> allSubjects, Map<Long, AlgoTeacher> globalTeacherMap, List<AlgoRoom> algoRooms, CodeValue academicYear, List<String> majors) {
        String yearLabel = switch (yearCode) { case "THIRD_YEAR" -> "Year 3"; case "FOURTH_YEAR" -> "Year 4"; default -> yearCode; };
        List<AlgoSubject> yearSubjects = filterAndMapSubjects(allSubjects, yearCode, isFirstSem, globalTeacherMap);

        for (String major : majors) {
            processSection(String.format("%s (%s) - Section A", yearLabel, major), yearSubjects, algoRooms, academicYear);
        }
    }

    private void processSection(String sectionName, List<AlgoSubject> sectionSubjects, List<AlgoRoom> algoRooms, CodeValue academicYear) {
        log.info("--- Processing {} ---", sectionName);
        long start = System.currentTimeMillis();
        Optional<MajorSection> sectionOpt = majorSectionRepo.findByName(sectionName);
        if (sectionOpt.isEmpty()) {
            log.warn("Skipping {}: Not found in DB.", sectionName);
            return;
        }
        MajorSection majorSection = sectionOpt.get();
        boolean sectionSolved = false;
        int attempts = 0;
        while (!sectionSolved && attempts < 100) {
            attempts++;
            for (AlgoSubject s : sectionSubjects) s.reset();
            ScheduledSlot[] rawSchedule = new ScheduledSlot[30];
            Set<Integer> freeSlots = generateBalancedFreeSlots(30);
            if (solve(0, rawSchedule, sectionSubjects, algoRooms, freeSlots)) {
                log.info("    [SUCCESS] Solved {} (Attempt #{}, {}ms)", sectionName, attempts, (System.currentTimeMillis() - start));
                saveToDatabase(rawSchedule, majorSection, academicYear, freeSlots);
                sectionSolved = true;
            }
        }
        if (!sectionSolved) {
            log.error("!!! FAILED to solve {}. Resources Exhausted.", sectionName);
        }
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
        int morningFree = 0; int eveningFree = 0;
        while (slots.size() < 6) {
            int r = rand.nextInt(totalSlots);
            if (slots.contains(r-1) && slots.contains(r-2)) continue;
            if (slots.contains(r+1) && slots.contains(r+2)) continue;
            boolean isMorning = (r % 6) < 3;
            if (isMorning && morningFree >= 3) continue;
            if (!isMorning && eveningFree >= 3) continue;
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

        List<CodeValue> allDays = codeValueRepo.findByCode_Name("Timetable Days");
        List<CodeValue> allPeriods = codeValueRepo.findByCode_Name("Timetable Periods");

        if(allDays.isEmpty() || allPeriods.isEmpty()) {
            throw new RuntimeException("DB Configuration Error: 'Timetable Days' or 'Timetable Periods' not found in code_value table.");
        }

        for (int slot = 0; slot < 30; slot++) {
            if (freeSlots.contains(slot) || rawSchedule[slot] == null) continue;
            ScheduledSlot result = rawSchedule[slot];
            TimetableData data = new TimetableData();
            data.setTimetableDay(allDays.get(slot / 6));
            data.setTimetablePeriod(allPeriods.get(slot % 6));
            data.setSubject(subjectRepo.getReferenceById(result.getSubject().getDbId()));
            data.setRoom(roomRepo.getReferenceById(result.getRoom().getDbId()));
            data.setTeacher(profileRepo.getReferenceById(result.getAssignedTeacher().getId()));
            data = timetableDataRepo.save(data);
            Timetable timetable = new Timetable();
            timetable.setTimetableInfo(info);
            timetable.setTimetableData(data);
            timetableRepo.save(timetable);
        }
    }
}