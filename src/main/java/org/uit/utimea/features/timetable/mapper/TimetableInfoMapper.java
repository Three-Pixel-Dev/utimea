package org.uit.utimea.features.timetable.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.uit.utimea.shared.mapper.MasterDataMapper;
import org.uit.utimea.shared.entity.TimetableInfo;
import org.uit.utimea.features.timetable.dto.request.TimetableInfoRequest;
import org.uit.utimea.features.timetable.dto.response.*;
import org.uit.utimea.features.timetable.util.TimetableUtil;
import org.uit.utimea.shared.repository.*;

@Component
@RequiredArgsConstructor
public class TimetableInfoMapper {

    private final MasterDataMapper masterDataMapper;
    private final TimetableInfoRepository timetableInfoRepository;
    private final MajorSectionRepository majorSectionRepository;
    private final CodeValueRepository codeValueRepository;

    public TimetableInfo toEntity(TimetableInfoRequest request) {
        var majorSection = majorSectionRepository.findById(request.majorSectionId())
                .orElseThrow(() -> new RuntimeException("MajorSection not found with id: " + request.majorSectionId()));

        var academicYear = codeValueRepository.findById(request.academicYearId())
                .orElseThrow(() -> new RuntimeException("CodeValue not found with id: " + request.academicYearId()));

        TimetableInfo newInfo = TimetableInfo.builder()
                .majorSection(majorSection)
                .academicYear(academicYear)
                .build();
        
        String name = TimetableUtil.generateTimetableName(newInfo);
        newInfo.setName(name);

        return timetableInfoRepository.save(newInfo);
    }

    public TimetableInfoResponse toResponse(TimetableInfo entity) {
        if (entity == null) {
            return null;
        }

        MajorSectionResponse majorSectionResponse = MajorSectionResponse.builder()
                .id(entity.getMajorSection().getId())
                .name(entity.getMajorSection().getName())
                .build();

        CodeValueResponse academicYearResponse = CodeValueResponse.builder()
                .id(entity.getAcademicYear().getId())
                .name(entity.getAcademicYear().getName())
                .build();

        return TimetableInfoResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .majorSection(majorSectionResponse)
                .academicYear(academicYearResponse)
                .masterData(masterDataMapper.toMasterData(entity))
                .build();
    }

    public void updateEntity(TimetableInfo entity, TimetableInfoRequest request) {
        var majorSection = majorSectionRepository.findById(request.majorSectionId())
                .orElseThrow(() -> new RuntimeException("MajorSection not found with id: " + request.majorSectionId()));

        var academicYear = codeValueRepository.findById(request.academicYearId())
                .orElseThrow(() -> new RuntimeException("CodeValue not found with id: " + request.academicYearId()));

        entity.setMajorSection(majorSection);
        entity.setAcademicYear(academicYear);
        
        String name = TimetableUtil.generateTimetableName(entity);
        entity.setName(name);
    }
}
