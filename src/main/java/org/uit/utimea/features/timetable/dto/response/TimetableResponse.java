package org.uit.utimea.features.timetable.dto.response;

import lombok.Builder;
import org.uit.utimea.shared.dto.response.MasterData;

@Builder
public record TimetableResponse(
        Long id,
        String name,
        TimetableInfoResponse timetableInfo,
        TimetableDataResponse timetableData,
        MasterData masterData
) {}
