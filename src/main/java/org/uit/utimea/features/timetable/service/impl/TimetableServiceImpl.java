package org.uit.utimea.features.timetable.service.impl;

import org.springframework.stereotype.Service;
import org.uit.utimea.shared.service.impl.BaseServiceImpl;
import org.uit.utimea.shared.entity.Timetable;
import org.uit.utimea.features.timetable.dto.request.TimetableFilter;
import org.uit.utimea.features.timetable.dto.request.TimetableRequest;
import org.uit.utimea.features.timetable.dto.response.TimetableResponse;
import org.uit.utimea.features.timetable.mapper.TimetableMapper;
import org.uit.utimea.features.timetable.service.TimetableService;
import org.uit.utimea.shared.repository.TimetableRepository;

import java.util.Map;

@Service
public class TimetableServiceImpl extends BaseServiceImpl<Timetable, TimetableRequest, TimetableResponse, TimetableFilter> implements TimetableService {

    private final TimetableMapper timetableMapper;

    public TimetableServiceImpl(TimetableRepository timetableRepository, TimetableMapper timetableMapper) {
        super(timetableRepository);
        this.timetableMapper = timetableMapper;
    }

    @Override
    protected Timetable mapRequestToEntity(TimetableRequest request) {
        return timetableMapper.toEntity(request);
    }

    @Override
    protected TimetableResponse mapEntityToResponse(Timetable entity) {
        return timetableMapper.toResponse(entity);
    }

    @Override
    protected void updateEntityFromRequest(Timetable entity, TimetableRequest request) {
        timetableMapper.updateEntity(entity, request);
    }

    @Override
    protected Map<String, String> getFieldMapping() {
        return Map.of(
                "academicYearId", "timetableInfo.academicYear.id",
                "majorSectionId", "timetableInfo.majorSection.id",
                "timetableDayId", "timetableData.timetableDay.id",
                "timetablePeriodId", "timetableData.timetablePeriod.id",
                "subjectId", "timetableData.subject.id",
                "roomId", "timetableData.room.id"
        );
    }
}
