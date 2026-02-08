package org.uit.utimea.features.timetable.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.uit.utimea.shared.mapper.MasterDataMapper;
import org.uit.utimea.shared.entity.*;
import org.uit.utimea.features.timetable.dto.request.TimetableRequest;
import org.uit.utimea.features.timetable.dto.response.*;
import org.uit.utimea.features.timetable.util.TimetableUtil;
import org.uit.utimea.shared.repository.*;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TimetableMapper {

    private final MasterDataMapper masterDataMapper;
    private final TimetableInfoRepository timetableInfoRepository;
    private final TimetableDataRepository timetableDataRepository;
    private final MajorSectionRepository majorSectionRepository;
    private final CodeValueRepository codeValueRepository;
    private final SubjectRepository subjectRepository;
    private final RoomRepository roomRepository;

    public Timetable toEntity(TimetableRequest request) {
        TimetableInfo timetableInfo = findOrCreateTimetableInfo(
                request.majorSectionId(),
                request.academicYearId()
        );

        TimetableData timetableData = findOrCreateTimetableData(
                request.timetableDayId(),
                request.timetablePeriodId(),
                request.subjectId(),
                request.roomId()
        );

        return Timetable.builder()
                .timetableInfo(timetableInfo)
                .timetableData(timetableData)
                .build();
    }

    public TimetableResponse toResponse(Timetable entity) {
        if (entity == null) {
            return null;
        }

        TimetableInfoResponse timetableInfoResponse = mapTimetableInfoToResponse(entity.getTimetableInfo());
        TimetableDataResponse timetableDataResponse = mapTimetableDataToResponse(entity.getTimetableData());

        return TimetableResponse.builder()
                .id(entity.getId())
                .name(entity.getTimetableInfo() != null ? entity.getTimetableInfo().getName() : null)
                .timetableInfo(timetableInfoResponse)
                .timetableData(timetableDataResponse)
                .masterData(masterDataMapper.toMasterData(entity))
                .build();
    }

    public void updateEntity(Timetable entity, TimetableRequest request) {
        TimetableInfo timetableInfo = findOrCreateTimetableInfo(
                request.majorSectionId(),
                request.academicYearId()
        );

        TimetableData timetableData = findOrCreateTimetableData(
                request.timetableDayId(),
                request.timetablePeriodId(),
                request.subjectId(),
                request.roomId()
        );

        entity.setTimetableInfo(timetableInfo);
        entity.setTimetableData(timetableData);
    }

    private TimetableInfo findOrCreateTimetableInfo(Long majorSectionId, Long academicYearId) {
        MajorSection majorSection = majorSectionRepository.findById(majorSectionId)
                .orElseThrow(() -> new RuntimeException("MajorSection not found with id: " + majorSectionId));

        CodeValue academicYear = codeValueRepository.findById(academicYearId)
                .orElseThrow(() -> new RuntimeException("CodeValue not found with id: " + academicYearId));

        List<TimetableInfo> existing = timetableInfoRepository.findAll().stream()
                .filter(info -> info.getMajorSection().getId().equals(majorSectionId) &&
                        info.getAcademicYear().getId().equals(academicYearId))
                .toList();

        if (!existing.isEmpty()) {
            TimetableInfo existingInfo = existing.get(0);
            // Ensure name is set for existing records
            if (existingInfo.getName() == null || existingInfo.getName().isEmpty()) {
                String name = TimetableUtil.generateTimetableName(existingInfo);
                existingInfo.setName(name);
                return timetableInfoRepository.save(existingInfo);
            }
            return existingInfo;
        }

        TimetableInfo newInfo = TimetableInfo.builder()
                .majorSection(majorSection)
                .academicYear(academicYear)
                .build();
        
        String name = TimetableUtil.generateTimetableName(newInfo);
        newInfo.setName(name);

        return timetableInfoRepository.save(newInfo);
    }

    private TimetableData findOrCreateTimetableData(Long timetableDayId, Long timetablePeriodId,
                                                     Long subjectId, Long roomId) {
        CodeValue timetableDay = codeValueRepository.findById(timetableDayId)
                .orElseThrow(() -> new RuntimeException("CodeValue not found with id: " + timetableDayId));

        CodeValue timetablePeriod = codeValueRepository.findById(timetablePeriodId)
                .orElseThrow(() -> new RuntimeException("CodeValue not found with id: " + timetablePeriodId));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found with id: " + subjectId));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        List<TimetableData> existing = timetableDataRepository.findAll().stream()
                .filter(data -> data.getTimetableDay().getId().equals(timetableDayId) &&
                        data.getTimetablePeriod().getId().equals(timetablePeriodId) &&
                        data.getSubject().getId().equals(subjectId) &&
                        data.getRoom().getId().equals(roomId))
                .toList();

        if (!existing.isEmpty()) {
            return existing.get(0);
        }

        TimetableData newData = TimetableData.builder()
                .timetableDay(timetableDay)
                .timetablePeriod(timetablePeriod)
                .subject(subject)
                .room(room)
                .build();

        return timetableDataRepository.save(newData);
    }

    private TimetableInfoResponse mapTimetableInfoToResponse(TimetableInfo info) {
        if (info == null) {
            return null;
        }

        MajorSectionResponse majorSectionResponse = MajorSectionResponse.builder()
                .id(info.getMajorSection().getId())
                .name(info.getMajorSection().getName())
                .build();

        CodeValueResponse academicYearResponse = CodeValueResponse.builder()
                .id(info.getAcademicYear().getId())
                .name(info.getAcademicYear().getName())
                .build();

        return TimetableInfoResponse.builder()
                .id(info.getId())
                .name(info.getName())
                .majorSection(majorSectionResponse)
                .academicYear(academicYearResponse)
                .build();
    }

    private TimetableDataResponse mapTimetableDataToResponse(TimetableData data) {
        if (data == null) {
            return null;
        }

        CodeValueResponse dayResponse = CodeValueResponse.builder()
                .id(data.getTimetableDay().getId())
                .name(data.getTimetableDay().getName())
                .build();

        CodeValueResponse periodResponse = CodeValueResponse.builder()
                .id(data.getTimetablePeriod().getId())
                .name(data.getTimetablePeriod().getName())
                .build();

        SubjectResponse subjectResponse = SubjectResponse.builder()
                .id(data.getSubject().getId())
                .code(data.getSubject().getCode())
                .description(data.getSubject().getDescription())
                .build();

        RoomResponse roomResponse = RoomResponse.builder()
                .id(data.getRoom().getId())
                .name(data.getRoom().getName())
                .capacity(data.getRoom().getCapacity())
                .build();

        return TimetableDataResponse.builder()
                .id(data.getId())
                .timetableDay(dayResponse)
                .timetablePeriod(periodResponse)
                .subject(subjectResponse)
                .room(roomResponse)
                .build();
    }
}
