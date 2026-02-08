package org.uit.utimea.features.room.dto.request;

public record RoomRequest(
        String name,
        Integer capacity,
        Long roomTypeId
) {}
