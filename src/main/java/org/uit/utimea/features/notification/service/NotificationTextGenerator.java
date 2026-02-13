package org.uit.utimea.features.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.uit.utimea.shared.entity.*;
import org.uit.utimea.shared.repository.*;

@Service
@RequiredArgsConstructor
public class NotificationTextGenerator {

    private final TimetableInfoRepository timetableInfoRepository;
    private final TimetableDataRepository timetableDataRepository;
    private final TimetableRepository timetableRepository;
    private final MajorSectionRepository majorSectionRepository;
    private final ProfileRepository profileRepository;
    private final RoomRepository roomRepository;
    private final CodeValueRepository codeValueRepository;
    private final SubjectRepository subjectRepository;

    public String generateReadableText(String action, Long teacherId, Long timetableInfoId, Long majorSectionId) {
        try {
            switch (action) {
                case "TIMETABLE_UPDATE":
                    return generateTimetableUpdateText(timetableInfoId, majorSectionId);
                case "COMBINE_CLASS":
                    return generateCombineClassText(timetableInfoId, majorSectionId);
                case "ROOM_CHANGE_APPROVED":
                    return generateRoomChangeText(timetableInfoId, teacherId, majorSectionId, true);
                case "ROOM_CHANGE_DECLINED":
                    return generateRoomChangeText(timetableInfoId, teacherId, majorSectionId, false);
                case "PERIOD_CHANGE_APPROVED":
                    return generatePeriodChangeText(timetableInfoId, teacherId, majorSectionId, true);
                case "PERIOD_CHANGE_DECLINED":
                    return generatePeriodChangeText(timetableInfoId, teacherId, majorSectionId, false);
                default:
                    return "Timetable change notification";
            }
        } catch (Exception e) {
            return "Timetable change notification";
        }
    }

    private String generateTimetableUpdateText(Long timetableInfoId, Long majorSectionId) {
        TimetableInfo timetableInfo = timetableInfoRepository.findById(timetableInfoId)
                .orElse(null);
        MajorSection majorSection = majorSectionRepository.findById(majorSectionId)
                .orElse(null);

        if (timetableInfo != null && majorSection != null) {
            return String.format("Timetable has been updated for %s - %s",
                    majorSection.getName(),
                    timetableInfo.getName() != null ? timetableInfo.getName() : "Timetable");
        }
        return "Timetable has been updated";
    }

    private String generateCombineClassText(Long timetableInfoId, Long majorSectionId) {
        TimetableInfo timetableInfo = timetableInfoRepository.findById(timetableInfoId)
                .orElse(null);
        MajorSection majorSection = majorSectionRepository.findById(majorSectionId)
                .orElse(null);

        if (timetableInfo != null && majorSection != null) {
            return String.format("Classes have been combined for %s - %s",
                    majorSection.getName(),
                    timetableInfo.getName() != null ? timetableInfo.getName() : "Timetable");
        }
        return "Classes have been combined";
    }

    private String generateRoomChangeText(Long timetableInfoId, Long teacherId, Long majorSectionId, boolean approved) {
        TimetableInfo timetableInfo = timetableInfoRepository.findById(timetableInfoId)
                .orElse(null);
        MajorSection majorSection = majorSectionRepository.findById(majorSectionId)
                .orElse(null);
        Profile teacher = teacherId != null ? profileRepository.findById(teacherId).orElse(null) : null;

        String status = approved ? "approved" : "declined";
        String teacherName = teacher != null ? teacher.getName() : "teacher";
        String sectionName = majorSection != null ? majorSection.getName() : "section";
        String timetableName = timetableInfo != null && timetableInfo.getName() != null 
                ? timetableInfo.getName() : "timetable";

        if (approved) {
            return String.format("Room change request has been %s for %s - %s (%s)",
                    status, sectionName, timetableName, teacherName);
        } else {
            return String.format("Room change request has been %s for %s - %s (%s)",
                    status, sectionName, timetableName, teacherName);
        }
    }

    private String generatePeriodChangeText(Long timetableInfoId, Long teacherId, Long majorSectionId, boolean approved) {
        TimetableInfo timetableInfo = timetableInfoRepository.findById(timetableInfoId)
                .orElse(null);
        MajorSection majorSection = majorSectionRepository.findById(majorSectionId)
                .orElse(null);
        Profile teacher = teacherId != null ? profileRepository.findById(teacherId).orElse(null) : null;

        String status = approved ? "approved" : "declined";
        String teacherName = teacher != null ? teacher.getName() : "teacher";
        String sectionName = majorSection != null ? majorSection.getName() : "section";
        String timetableName = timetableInfo != null && timetableInfo.getName() != null 
                ? timetableInfo.getName() : "timetable";

        if (approved) {
            return String.format("Period change request has been %s for %s - %s (%s)",
                    status, sectionName, timetableName, teacherName);
        } else {
            return String.format("Period change request has been %s for %s - %s (%s)",
                    status, sectionName, timetableName, teacherName);
        }
    }
}
