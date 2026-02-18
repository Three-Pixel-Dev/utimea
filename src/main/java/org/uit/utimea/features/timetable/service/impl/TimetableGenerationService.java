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
        int usedMorningSlots = 0;
        int usedEveningSlots = 0;
        int usedSpecialRooms = 0;

        // Locks a specific teacher to this section so they teach all 4 periods
        AlgoTeacher assignedTeacherForSection = null;

        public AlgoSubject(Long dbId, String code, String name, List<AlgoTeacher> teachers, int specialRoomCount, boolean requiresComputerRoom) {
            this.dbId = dbId;
            this.code = code;
            this.name = name;
            this.teachers = teachers;
            this.specialRoomCount = specialRoomCount;
            this.requiresComputerRoom = requiresComputerRoom;
        }

        public void reset() {
            usedMorningSlots = 0;
            usedEveningSlots = 0;
            usedSpecialRooms = 0;
            assignedTeacherForSection = null;
        }
            // java pc room, myanmrt normal
        public int getPriorityScore() {
            int score = 0;
            if (requiresComputerRoom) score += 200;
            if (specialRoomCount > 0) score += 50;
            return score;
        }

        public boolean isHeavy() {
            return requiresComputerRoom || specialRoomCount > 0;
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

        CodeValue selectedAcademicYear = codeValueRepo.findById(request.getAcademicYearId())
                .orElseThrow(() -> new RuntimeException("Academic Year not found with ID: " + request.getAcademicYearId()));

        boolean isFirstSem = "FIRST_SEM".equalsIgnoreCase(request.getSem());
        log.info(">>> STARTING GENERATION: Year ID={}, Sem={}, Name={}",
                request.getAcademicYearId(), request.getSem(), selectedAcademicYear.getName());

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

        // Year 1 & 2 (Always generated if students exist)
        if (hasStudents(request.getNumberOfStudentsInFirstYear())) {
            generateSectionBasedSchedule("FIRST_YEAR", request.getNumberOfStudentsInFirstYear(), isFirstSem,
                    dbSubjects, globalTeacherMap, algoRooms, selectedAcademicYear);
        }
        if (hasStudents(request.getNumberOfStudentsInSecondYear())) {
            generateSectionBasedSchedule("SECOND_YEAR", request.getNumberOfStudentsInSecondYear(), isFirstSem,
                    dbSubjects, globalTeacherMap, algoRooms, selectedAcademicYear);
        }

        // Major List
        List<String> majors = Arrays.asList("SE", "KE", "HPC", "BIS", "CN", "ES", "CSec");

        if (isFirstSem) {
            // First Semester: Generate 3rd and 4th Year
            if (hasStudents(request.getNumberOfStudentInThirdYear())) {
                generateMajorBasedSchedule("THIRD_YEAR", 0L, true, dbSubjects, globalTeacherMap, algoRooms, majors, selectedAcademicYear);
            }
            generateMajorBasedSchedule("FOURTH_YEAR", 0L, true, dbSubjects, globalTeacherMap, algoRooms, majors, selectedAcademicYear);
        } else {
            // Second Sem: Generate 3rd Year ONLY
            if (hasStudents(request.getNumberOfStudentInThirdYear())) {
                Map<String, Integer> y3Counts = request.getThirdYearMajorCounts();
                if (y3Counts == null || y3Counts.isEmpty()) y3Counts = createDefaultCounts(majors);
                generateSem2CombinedSchedule("THIRD_YEAR", y3Counts, dbSubjects, globalTeacherMap, algoRooms, selectedAcademicYear);
            }
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

    // --- SEMESTER RESOLUTION HELPERS ---

    private String getSemesterRoman(String yearCode, boolean isFirstSem) {
        if (isFirstSem) {
            return switch (yearCode) {
                case "FIRST_YEAR" -> "(I)";
                case "SECOND_YEAR" -> "(III)";
                case "THIRD_YEAR" -> "(V)";
                case "FOURTH_YEAR" -> "(VII)";
                default -> "(I)";
            };
        } else {
            return switch (yearCode) {
                case "FIRST_YEAR" -> "(II)";
                case "SECOND_YEAR" -> "(IV)";
                case "THIRD_YEAR" -> "(VI)";
                case "FOURTH_YEAR" -> "(VIII)";
                default -> "(II)";
            };
        }
    }

    // --- STRATEGY 3: SEMESTER 2 (COMBINED MAJORS) ---
    private void generateSem2CombinedSchedule(String yearCode, Map<String, Integer> majorCounts,
                                              List<Subject> allSubjects, Map<Long, AlgoTeacher> globalTeacherMap,
                                              List<AlgoRoom> algoRooms, CodeValue academicYear) {

        // Determine correct roman numeral based on year
        String roman = getSemesterRoman(yearCode, false); // false = Second Sem

        String yearLabel = switch (yearCode) {
            case "THIRD_YEAR" -> "Year 3";
            case "FOURTH_YEAR" -> "Year 4";
            default -> yearCode;
        };
        log.info("Generating Combined Schedule for {} ({})...", yearLabel, roman);

        List<Subject> semesterSubjects = allSubjects.stream()
                .filter(s -> s.getSubjectYear().name().equalsIgnoreCase(yearCode))
                .filter(s -> s.getIsFirstSem() != null && !s.getIsFirstSem())
                .collect(Collectors.toList());

        Map<String, List<String>> subjectToMajors = new HashMap<>();

        // --- SPECIFIC MAPPING FOR Y3 SEM 2 ---
        Map<String, List<String>> specialMappings = new HashMap<>();
        specialMappings.put("CS-6117", Arrays.asList("SE", "KE"));
        specialMappings.put("CS-6211", Arrays.asList("SE", "KE"));
        specialMappings.put("CS-6317", Arrays.asList("SE", "BIS"));
        specialMappings.put("CST-6114", Arrays.asList("SE", "KE", "HPC", "CN", "CSec"));
        specialMappings.put("CST-6210", Arrays.asList("HPC", "CN", "CSec"));
        specialMappings.put("CT-6415", Arrays.asList("CN", "CSec"));

        List<String> allMajorsList = Arrays.asList("SE", "KE", "HPC", "BIS", "CN", "ES", "CSec");
        specialMappings.put("CST-6316", allMajorsList);
        specialMappings.put("CST-6506", allMajorsList);

        for (Subject s : semesterSubjects) {
            String code = s.getCode();
            if (specialMappings.containsKey(code)) {
                List<String> requiredBy = specialMappings.get(code);
                List<String> activeMajors = new ArrayList<>();
                for (String m : requiredBy) {
                    if (majorCounts.containsKey(m)) activeMajors.add(m);
                }
                if (!activeMajors.isEmpty()) subjectToMajors.put(code, activeMajors);
            } else {
                String prefix = code.split("-")[0];
                if (majorCounts.containsKey(prefix)) {
                    subjectToMajors.put(code, Collections.singletonList(prefix));
                } else if (code.startsWith("CST-") || code.startsWith("CS-")) {
                    subjectToMajors.put(code, new ArrayList<>(majorCounts.keySet()));
                }
            }
        }

        Map<String, ScheduledSlot[]> majorTimetables = new HashMap<>();
        for (String major : majorCounts.keySet()) {
            majorTimetables.put(major, new ScheduledSlot[35]); // Scaled to 35
        }

        // Shuffle subjects for randomness
        Collections.shuffle(semesterSubjects);

        for (Subject sub : semesterSubjects) {
            List<String> takingMajors = subjectToMajors.getOrDefault(sub.getCode(), Collections.emptyList());
            if (takingMajors.isEmpty()) continue;

            // --- SMART GROUPING LOGIC ---
            List<List<String>> sections = new ArrayList<>();
            List<String> currentSection = new ArrayList<>();
            int currentSectionCount = 0;

            for (String major : takingMajors) {
                int count = majorCounts.getOrDefault(major, 0);
                if (currentSectionCount + count <= 40) {
                    currentSection.add(major);
                    currentSectionCount += count;
                } else {
                    if (!currentSection.isEmpty()) sections.add(currentSection);
                    currentSection = new ArrayList<>();
                    currentSection.add(major);
                    currentSectionCount = count;
                }
            }
            if (!currentSection.isEmpty()) sections.add(currentSection);

            // --- SCHEDULE EACH SECTION ---
            List<AlgoTeacher> subjectTeachers = new ArrayList<>();
            for(Profile p : sub.getTeachers()) {
                AlgoTeacher at = globalTeacherMap.get(p.getId());
                if(at != null) subjectTeachers.add(at);
            }
            Collections.shuffle(subjectTeachers);

            if (subjectTeachers.isEmpty()) {
                log.error("No teachers found for {}", sub.getCode());
                continue;
            }

            AlgoSubject algoSub = mapToAlgoSubject(sub, globalTeacherMap);
            int teacherIndex = 0;

            for (List<String> sectionMajors : sections) {
                AlgoTeacher assignedTeacher = subjectTeachers.get(teacherIndex % subjectTeachers.size());
                teacherIndex++;

                int slotsRequired = 4;
                int slotsBooked = 0;

                int morningBooked = 0;
                int eveningBooked = 0;
                int specialRoomsBooked = 0;

                List<Integer> slotOrder = new ArrayList<>();
                for(int k=0; k<35; k++) {
                    if (k % 7 != 3) slotOrder.add(k); // SKIP LUNCH
                }
                Collections.shuffle(slotOrder);

                for (int slot : slotOrder) {
                    if (slotsBooked >= slotsRequired) break;

                    boolean isMorning = (slot % 7) < 3;
                    if (isMorning && morningBooked >= 2) continue;
                    if (!isMorning && eveningBooked >= 2) continue;

                    // 1. Check Major Availability
                    boolean majorsFree = true;
                    for (String major : sectionMajors) {
                        if (majorTimetables.get(major)[slot] != null) {
                            majorsFree = false;
                            break;
                        }
                    }
                    if (!majorsFree) continue;

                    // 2. Check Daily Limit
                    if (isMajorDailyLimitExceeded(majorTimetables, sectionMajors.get(0), slot, algoSub.getDbId())) continue;

                    // 3. HEAVY SUBJECT CHECK
                    if (algoSub.isHeavy()) {
                        if (isMajorDailyHeavyLimitExceeded(majorTimetables, sectionMajors.get(0), slot)) continue;
                    }

                    // 4. Check Teacher Availability
                    if (isTeacherBusyOrExhausted(assignedTeacher, slot, majorTimetables, sectionMajors)) continue;

                    // 5. Check Special Room Requirements
                    boolean requiresSpecialThisSlot = false;
                    int remainingSpecial = algoSub.specialRoomCount - specialRoomsBooked;

                    if (remainingSpecial > 0) {
                        if (!isMorning) {
                            requiresSpecialThisSlot = true;
                        } else if (remainingSpecial > (2 - eveningBooked)) {
                            requiresSpecialThisSlot = true;
                        }
                    }

                    boolean lookingForComputerRoom = requiresSpecialThisSlot && algoSub.requiresComputerRoom;
                    boolean lookingForSpecialRoom = requiresSpecialThisSlot && !algoSub.requiresComputerRoom;

                    // 6. Check Room Availability
                    AlgoRoom bookedRoom = null;
                    List<AlgoRoom> shuffledRooms = new ArrayList<>(algoRooms);
                    Collections.shuffle(shuffledRooms);
                    for (AlgoRoom r : shuffledRooms) {
                        if (r.globalBusySlots.contains(slot)) continue;

                        if (lookingForComputerRoom) {
                            if (r.isComputerRoom) { bookedRoom = r; break; }
                        } else if (lookingForSpecialRoom) {
                            if (r.isSpecialRoom) { bookedRoom = r; break; }
                        } else {
                            if (!r.isComputerRoom && !r.isSpecialRoom) { bookedRoom = r; break; }
                        }
                    }
                    if (bookedRoom == null) continue;

                    assignedTeacher.globalBusySlots.add(slot);
                    bookedRoom.globalBusySlots.add(slot);

                    String typeTag = isMorning ? "(L)" : "(TDA)";
                    if (lookingForComputerRoom) typeTag = "(PC)";
                    else if (lookingForSpecialRoom) typeTag = "(LAB)";

                    ScheduledSlot scheduledSlot = new ScheduledSlot(algoSub, bookedRoom, assignedTeacher, typeTag);

                    for (String major : sectionMajors) {
                        majorTimetables.get(major)[slot] = scheduledSlot;
                    }

                    slotsBooked++;
                    if (isMorning) morningBooked++; else eveningBooked++;
                    if (lookingForComputerRoom || lookingForSpecialRoom) specialRoomsBooked++;
                }

                if (slotsBooked < slotsRequired) {
                    log.warn("Could not fully schedule {} for group {}. Booked {}/4 slots.", sub.getCode(), sectionMajors, slotsBooked);
                }
            }
        }

        for (String major : majorCounts.keySet()) {
            String sectionName = String.format("%s (%s) - Section A", yearLabel, major);
            saveToDatabaseWithArray(majorTimetables.get(major), sectionName, academicYear, roman);
        }
    }

    private boolean isMajorDailyLimitExceeded(Map<String, ScheduledSlot[]> majorTimetables, String major, int slot, Long subjectId) {
        ScheduledSlot[] schedule = majorTimetables.get(major);
        int dayStart = (slot / 7) * 7;
        int count = 0;
        for (int k = dayStart; k < dayStart + 7; k++) {
            if (schedule[k] != null && schedule[k].getSubject().getDbId().equals(subjectId)) {
                count++;
            }
        }
        return count >= 2;
    }

    private boolean isMajorDailyHeavyLimitExceeded(Map<String, ScheduledSlot[]> majorTimetables, String major, int slot) {
        ScheduledSlot[] schedule = majorTimetables.get(major);
        int dayStart = (slot / 7) * 7;
        int heavyCount = 0;
        for (int k = dayStart; k < dayStart + 7; k++) {
            if (schedule[k] != null && schedule[k].getSubject().isHeavy()) {
                heavyCount++;
            }
        }
        return heavyCount >= 2;
    }

    private boolean isTeacherBusyOrExhausted(AlgoTeacher teacher, int slot, Map<String, ScheduledSlot[]> timetables, List<String> currentMajors) {
        if (teacher.globalBusySlots.contains(slot)) return true;

        int dayIndex = slot / 7;
        int periodIndex = slot % 7;

        if (periodIndex == 6) {
            int firstSlotOfDay = (dayIndex * 7);
            if (teacher.globalBusySlots.contains(firstSlotOfDay)) return true;
        }
        if (periodIndex == 0) {
            int lastSlotOfDay = (dayIndex * 7) + 6;
            if (teacher.globalBusySlots.contains(lastSlotOfDay)) return true;
        }

        return false;
    }

    private void saveToDatabaseWithArray(ScheduledSlot[] rawSchedule, String sectionName, CodeValue year, String semesterRoman) {
        Optional<MajorSection> sectionOpt = majorSectionRepo.findByName(sectionName);
        if (sectionOpt.isEmpty()) return;
        saveToDatabase(rawSchedule, sectionOpt.get(), year, new HashSet<>(), semesterRoman);
    }

    private void generateSectionBasedSchedule(String yearCode, Long studentCount, boolean isFirstSem, List<Subject> allSubjects, Map<Long, AlgoTeacher> globalTeacherMap, List<AlgoRoom> algoRooms, CodeValue academicYear) {
        int numberOfSections = (int) Math.ceil((double) studentCount / 40);
        String yearLabel = yearCode.equals("FIRST_YEAR") ? "Year 1" : "Year 2";
        List<AlgoSubject> yearSubjects = filterAndMapSubjects(allSubjects, yearCode, isFirstSem, globalTeacherMap);

        // Determine correct roman numeral
        String roman = getSemesterRoman(yearCode, isFirstSem);

        for (int i = 0; i < numberOfSections; i++) {
            char sectionChar = (char) ('A' + i);
            processSection(yearLabel + " - Section " + sectionChar, yearSubjects, algoRooms, academicYear, roman);
        }
    }

    private void generateMajorBasedSchedule(String yearCode, Long totalStudentCount, boolean isFirstSem, List<Subject> allSubjects, Map<Long, AlgoTeacher> globalTeacherMap, List<AlgoRoom> algoRooms, List<String> majors, CodeValue academicYear) {
        String yearLabel = switch (yearCode) { case "THIRD_YEAR" -> "Year 3"; case "FOURTH_YEAR" -> "Year 4"; default -> yearCode; };
        List<AlgoSubject> yearSubjects = filterAndMapSubjects(allSubjects, yearCode, isFirstSem, globalTeacherMap);

        String roman = getSemesterRoman(yearCode, isFirstSem);

        for (String major : majors) {
            processSection(String.format("%s (%s) - Section A", yearLabel, major), yearSubjects, algoRooms, academicYear, roman);
        }
    }

    private void processSection(String sectionName, List<AlgoSubject> sectionSubjects, List<AlgoRoom> algoRooms, CodeValue academicYear, String semesterRoman) {
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
            ScheduledSlot[] rawSchedule = new ScheduledSlot[35];

            Set<Integer> freeSlots = generateBalancedFreeSlots(35, sectionSubjects.size());

            Collections.shuffle(sectionSubjects);

            if (solve(0, rawSchedule, sectionSubjects, algoRooms, freeSlots)) {
                log.info("    [SUCCESS] Solved {} (Attempt #{}, {}ms)", sectionName, attempts, (System.currentTimeMillis() - start));
                saveToDatabase(rawSchedule, majorSection, academicYear, freeSlots, semesterRoman);
                sectionSolved = true;
            }
        }
        if (!sectionSolved) {
            log.error("!!! FAILED to solve {}. Resources Exhausted.", sectionName);
        }
    }

    private int getDailyCount(ScheduledSlot[] schedule, int currentSlot, AlgoSubject sub) {
        int dayStart = (currentSlot / 7) * 7;
        int count = 0;
        for (int k = dayStart; k < currentSlot; k++) {
            if (schedule[k] != null && schedule[k].getSubject().getDbId().equals(sub.getDbId())) {
                count++;
            }
        }
        return count;
    }

    private int getDailyHeavyCount(ScheduledSlot[] schedule, int currentSlot) {
        int dayStart = (currentSlot / 7) * 7;
        int heavyCount = 0;
        for (int k = dayStart; k < currentSlot; k++) {
            if (schedule[k] != null && schedule[k].getSubject().isHeavy()) {
                heavyCount++;
            }
        }
        return heavyCount;
    }

    private Set<Integer> generateBalancedFreeSlots(int totalSlots, int numSubjects) {
        Set<Integer> slots = new HashSet<>();

        // 1. PERMANENTLY LOCK LUNCH PERIODS (Period Index 3 for every Day)
        for (int i = 0; i < 5; i++) {
            slots.add((i * 7) + 3);
        }

        int requiredSlots = numSubjects * 4;
        int remainingFreeNeeded = totalSlots - 5 - requiredSlots;

        if (remainingFreeNeeded <= 0) return slots;

        Random rand = new Random();
        int morningFree = 0; int eveningFree = 0;
        int targetMorning = remainingFreeNeeded / 2;
        int targetEvening = remainingFreeNeeded - targetMorning;

        int attempts = 0;
        while (slots.size() < 5 + remainingFreeNeeded && attempts < 1000) {
            attempts++;
            int r = rand.nextInt(totalSlots);
            if (slots.contains(r)) continue;

            if (remainingFreeNeeded >= 6) {
                if (slots.contains(r-1) && slots.contains(r-2)) continue;
                if (slots.contains(r+1) && slots.contains(r+2)) continue;
            }

            boolean isMorning = (r % 7) < 3;

            if (isMorning && morningFree >= targetMorning && remainingFreeNeeded > 2) continue;
            if (!isMorning && eveningFree >= targetEvening && remainingFreeNeeded > 2) continue;

            if (slots.add(r)) {
                if (isMorning) morningFree++;
                else eveningFree++;
            }
        }

        while (slots.size() < 5 + remainingFreeNeeded) {
            slots.add(rand.nextInt(totalSlots));
        }

        return slots;
    }

    // --- RECURSIVE SOLVER (YEAR 1 & 2) ---
    private boolean solve(int slot, ScheduledSlot[] schedule, List<AlgoSubject> subjects, List<AlgoRoom> rooms, Set<Integer> freeSlots) {
        if (slot >= 35) {
            return subjects.stream().allMatch(s -> (s.usedMorningSlots + s.usedEveningSlots) >= 4);
        }

        if (freeSlots.contains(slot)) return solve(slot + 1, schedule, subjects, rooms, freeSlots);

        boolean isMorning = (slot % 7) < 3;

        List<AlgoSubject> sortedSubjects = new ArrayList<>(subjects);
        Collections.shuffle(sortedSubjects);
        sortedSubjects.sort((s1, s2) -> s2.getPriorityScore() - s1.getPriorityScore());

        for (AlgoSubject sub : sortedSubjects) {

            if (isMorning && sub.usedMorningSlots >= 2) continue;
            if (!isMorning && sub.usedEveningSlots >= 2) continue;

            if (getDailyCount(schedule, slot, sub) >= 2) continue;
            if (sub.isHeavy() && getDailyHeavyCount(schedule, slot) >= 2) continue;

            AlgoTeacher assignedTeacher = null;

            if (sub.assignedTeacherForSection != null) {
                assignedTeacher = sub.assignedTeacherForSection;
                if (slot % 7 == 6) {
                    int firstSlotOfDay = slot - 6;
                    if (schedule[firstSlotOfDay] != null &&
                            schedule[firstSlotOfDay].getAssignedTeacher().getId().equals(assignedTeacher.getId())) {
                        assignedTeacher = null;
                    }
                }
                if (assignedTeacher != null && assignedTeacher.globalBusySlots.contains(slot)) {
                    assignedTeacher = null;
                }
            } else {
                int minBusy = Integer.MAX_VALUE;
                List<AlgoTeacher> shuffledTeachers = new ArrayList<>(sub.teachers);
                Collections.shuffle(shuffledTeachers);

                for (AlgoTeacher t : shuffledTeachers) {
                    if (slot % 7 == 6) {
                        int firstSlotOfDay = slot - 6;
                        if (schedule[firstSlotOfDay] != null &&
                                schedule[firstSlotOfDay].getAssignedTeacher().getId().equals(t.getId())) {
                            continue;
                        }
                    }
                    if (!t.globalBusySlots.contains(slot)) {
                        if (t.globalBusySlots.size() < minBusy) {
                            minBusy = t.globalBusySlots.size();
                            assignedTeacher = t;
                        }
                    }
                }
            }

            if (assignedTeacher == null) continue;

            boolean requiresSpecialThisSlot = false;
            int remainingSpecial = sub.specialRoomCount - sub.usedSpecialRooms;

            if (remainingSpecial > 0) {
                if (!isMorning) {
                    requiresSpecialThisSlot = true;
                } else if (remainingSpecial > (2 - sub.usedEveningSlots)) {
                    requiresSpecialThisSlot = true;
                }
            }

            boolean lookingForComputerRoom = requiresSpecialThisSlot && sub.requiresComputerRoom;
            boolean lookingForSpecialRoom = requiresSpecialThisSlot && !sub.requiresComputerRoom;

            AlgoRoom assignedRoom = null;
            List<AlgoRoom> shuffledRooms = new ArrayList<>(rooms);
            Collections.shuffle(shuffledRooms);

            for (AlgoRoom r : shuffledRooms) {
                if (r.globalBusySlots.contains(slot)) continue;

                if (lookingForComputerRoom) {
                    if (r.isComputerRoom) { assignedRoom = r; break; }
                } else if (lookingForSpecialRoom) {
                    if (r.isSpecialRoom) { assignedRoom = r; break; }
                } else {
                    if (!r.isComputerRoom && !r.isSpecialRoom) { assignedRoom = r; break; }
                }
            }

            if (assignedRoom == null) continue;

            boolean isFirstPlacement = (sub.usedMorningSlots + sub.usedEveningSlots == 0);
            if (isFirstPlacement) {
                sub.assignedTeacherForSection = assignedTeacher;
            }

            assignedTeacher.globalBusySlots.add(slot);
            assignedRoom.globalBusySlots.add(slot);

            if(isMorning) sub.usedMorningSlots++; else sub.usedEveningSlots++;
            if(lookingForComputerRoom || lookingForSpecialRoom) sub.usedSpecialRooms++;

            String typeTag = isMorning ? "(L)" : "(TDA)";
            if (lookingForComputerRoom) typeTag = "(PC)";
            else if (lookingForSpecialRoom) typeTag = "(LAB)";

            schedule[slot] = new ScheduledSlot(sub, assignedRoom, assignedTeacher, typeTag);

            if (solve(slot + 1, schedule, subjects, rooms, freeSlots)) return true;

            schedule[slot] = null;
            assignedTeacher.globalBusySlots.remove(slot);
            assignedRoom.globalBusySlots.remove(slot);

            if(isMorning) sub.usedMorningSlots--; else sub.usedEveningSlots--;
            if(lookingForComputerRoom || lookingForSpecialRoom) sub.usedSpecialRooms--;

            if (isFirstPlacement) {
                sub.assignedTeacherForSection = null;
            }
        }

        return false;
    }

    private void saveToDatabase(ScheduledSlot[] rawSchedule, MajorSection section, CodeValue year, Set<Integer> freeSlots, String semesterRoman) {
        TimetableInfo info = new TimetableInfo();
        info.setMajorSection(section);
        info.setAcademicYear(year);
        info.setName(section.getName() + " (" + year.getName() + " Semester " + semesterRoman + ")");
        info = timetableInfoRepo.save(info);

        List<CodeValue> allDays = codeValueRepo.findByCode_Name("Timetable Days");
        List<CodeValue> allPeriods = codeValueRepo.findByCode_Name("Timetable Periods");

        if(allDays.isEmpty() || allPeriods.isEmpty()) {
            throw new RuntimeException("DB Configuration Error: 'Timetable Days' or 'Timetable Periods' not found in code_value table.");
        }

        for (int slot = 0; slot < 35; slot++) {
            if (freeSlots.contains(slot) || rawSchedule[slot] == null) continue;

            ScheduledSlot result = rawSchedule[slot];
            CodeValue day = allDays.get(slot / 7);
            Long subjectId = result.getSubject().getDbId();
            Long teacherId = result.getAssignedTeacher().getId();

            // Validate: Check if same subject and teacher already exist on the same day
            boolean duplicateExists = timetableDataRepo.existsBySubjectAndTeacherAndDay(
                    subjectId, teacherId, day.getId());
            
            if (duplicateExists) {
                Subject subject = subjectRepo.getReferenceById(subjectId);
                Profile teacher = profileRepo.getReferenceById(teacherId);
                throw new IllegalArgumentException(
                        String.format("The same subject (%s) and teacher (%s) cannot be assigned to the same day (%s) more than once",
                                subject.getCode(), teacher.getName(), day.getName()));
            }

            TimetableData data = new TimetableData();
            data.setTimetableDay(day);
            data.setTimetablePeriod(allPeriods.get(slot % 7));
            data.setSubject(subjectRepo.getReferenceById(subjectId));
            data.setRoom(roomRepo.getReferenceById(result.getRoom().getDbId()));
            data.setTeacher(profileRepo.getReferenceById(teacherId));
            data.setSubType(result.getTypeTag());
            data = timetableDataRepo.save(data);

            Timetable timetable = new Timetable();
            timetable.setTimetableInfo(info);
            timetable.setTimetableData(data);
            timetableRepo.save(timetable);
        }
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
        if (isFirstSem) {
            validationMultipliers.put("FOURTH_YEAR", 1);
        }

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
}