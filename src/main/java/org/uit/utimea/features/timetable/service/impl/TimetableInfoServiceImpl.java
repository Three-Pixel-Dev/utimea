package org.uit.utimea.features.timetable.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uit.utimea.shared.service.impl.BaseServiceImpl;
import org.uit.utimea.shared.entity.TimetableInfo;
import org.uit.utimea.features.timetable.dto.request.TimetableInfoFilter;
import org.uit.utimea.features.timetable.dto.request.TimetableInfoRequest;
import org.uit.utimea.features.timetable.dto.response.TimetableInfoResponse;
import org.uit.utimea.features.timetable.dto.response.TimetableInfoWithTimetablesResponse;
import org.uit.utimea.features.timetable.mapper.TimetableInfoMapper;
import org.uit.utimea.features.timetable.mapper.TimetableMapper;
import org.uit.utimea.features.timetable.service.TimetableInfoService;
import org.uit.utimea.features.timetable.util.TimetableUtil;
import org.uit.utimea.shared.repository.TimetableInfoRepository;
import org.uit.utimea.shared.repository.TimetableRepository;

import java.util.Map;

@Service
public class TimetableInfoServiceImpl extends BaseServiceImpl<TimetableInfo, TimetableInfoRequest, TimetableInfoResponse, TimetableInfoFilter> implements TimetableInfoService {

    private final TimetableInfoMapper timetableInfoMapper;
    private final TimetableMapper timetableMapper;
    private final TimetableRepository timetableRepository;
    
    public TimetableInfoServiceImpl(
            TimetableInfoRepository timetableInfoRepository, 
            TimetableInfoMapper timetableInfoMapper,
            TimetableMapper timetableMapper,
            TimetableRepository timetableRepository) {
        super(timetableInfoRepository);
        this.timetableInfoMapper = timetableInfoMapper;
        this.timetableMapper = timetableMapper;
        this.timetableRepository = timetableRepository;
    }

    @Override
    protected TimetableInfo mapRequestToEntity(TimetableInfoRequest request) {
        return timetableInfoMapper.toEntity(request);
    }

    @Override
    protected TimetableInfoResponse mapEntityToResponse(TimetableInfo entity) {
        // Generate name if not set (without saving, as this may be in a read-only context)
        if (entity.getName() == null || entity.getName().isEmpty()) {
            String name = TimetableUtil.generateTimetableName(entity);
            entity.setName(name);
        }
        return timetableInfoMapper.toResponse(entity);
    }

    @Override
    protected void updateEntityFromRequest(TimetableInfo entity, TimetableInfoRequest request) {
        timetableInfoMapper.updateEntity(entity, request);
    }

    @Override
    protected Map<String, String> getFieldMapping() {
        return Map.of(
                "majorSectionId", "majorSection.id",
                "academicYearId", "academicYear.id"
        );
    }

    @Override
    @Transactional
    public TimetableInfoWithTimetablesResponse findByIdWithTimetables(Long id) {
        TimetableInfo entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TimetableInfo not found with id: " + id));
        
        // Ensure name is set
        if (entity.getName() == null || entity.getName().isEmpty()) {
            String name = TimetableUtil.generateTimetableName(entity);
            entity.setName(name);
            entity = repository.save(entity);
        }
        
        TimetableInfoResponse infoResponse = timetableInfoMapper.toResponse(entity);
        
        // Get all timetables for this info with all relationships eagerly loaded
        var timetables = timetableRepository.findByTimetableInfo_IdWithAllRelations(id);
        var timetableResponses = timetables.stream()
                .map(timetableMapper::toResponse)
                .toList();
        
        return TimetableInfoWithTimetablesResponse.builder()
                .id(infoResponse.id())
                .name(infoResponse.name())
                .majorSection(infoResponse.majorSection())
                .academicYear(infoResponse.academicYear())
                .masterData(infoResponse.masterData())
                .timetables(timetableResponses)
                .build();
    }
}
