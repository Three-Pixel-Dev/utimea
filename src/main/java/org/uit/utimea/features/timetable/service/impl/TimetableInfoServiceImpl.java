package org.uit.utimea.features.timetable.service.impl;

import org.springframework.stereotype.Service;
import org.uit.utimea.shared.service.impl.BaseServiceImpl;
import org.uit.utimea.shared.entity.TimetableInfo;
import org.uit.utimea.features.timetable.dto.request.TimetableInfoFilter;
import org.uit.utimea.features.timetable.dto.request.TimetableInfoRequest;
import org.uit.utimea.features.timetable.dto.response.TimetableInfoResponse;
import org.uit.utimea.features.timetable.mapper.TimetableInfoMapper;
import org.uit.utimea.features.timetable.service.TimetableInfoService;
import org.uit.utimea.shared.repository.TimetableInfoRepository;

import java.util.Map;

@Service
public class TimetableInfoServiceImpl extends BaseServiceImpl<TimetableInfo, TimetableInfoRequest, TimetableInfoResponse, TimetableInfoFilter> implements TimetableInfoService {

    private final TimetableInfoMapper timetableInfoMapper;
    
    public TimetableInfoServiceImpl(TimetableInfoRepository timetableInfoRepository, TimetableInfoMapper timetableInfoMapper) {
        super(timetableInfoRepository);
        this.timetableInfoMapper = timetableInfoMapper;
    }

    @Override
    protected TimetableInfo mapRequestToEntity(TimetableInfoRequest request) {
        return timetableInfoMapper.toEntity(request);
    }

    @Override
    protected TimetableInfoResponse mapEntityToResponse(TimetableInfo entity) {
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
}
