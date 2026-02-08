package org.uit.utimea.features.timetable.dto.response;

import lombok.Builder;

@Builder
public record RoomResponse(
        Long id,
        String name,
        Integer capacity
) {}
