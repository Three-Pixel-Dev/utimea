package org.uit.utimea.features.room.dto.response;

import lombok.Builder;
import org.uit.utimea.shared.dto.response.MasterData;

@Builder
public record RoomResponse(
        Long id,
        String name,
        Integer capacity,
        CodeValueResponse roomType,
        MasterData masterData
) {}
