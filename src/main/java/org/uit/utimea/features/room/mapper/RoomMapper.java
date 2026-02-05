package org.uit.utimea.features.room.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.uit.utimea.shared.mapper.MasterDataMapper;
import org.uit.utimea.shared.entity.Room;
import org.uit.utimea.features.room.dto.request.RoomRequest;
import org.uit.utimea.features.room.dto.response.RoomResponse;

@Component
@RequiredArgsConstructor
public class RoomMapper {

    private final MasterDataMapper masterDataMapper;

    public Room toEntity(RoomRequest request) {
        return Room.builder()
                .name(request.name())
                .capacity(request.capacity())
                .type(request.type())
                .build();
    }

    public RoomResponse toResponse(Room entity) {
        if (entity == null) {
            return null;
        }
        return RoomResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .capacity(entity.getCapacity())
                .type(entity.getType())
                .masterData(masterDataMapper.toMasterData(entity))
                .build();
    }

    public void updateEntity(Room entity, RoomRequest request) {
        entity.setName(request.name());
        entity.setCapacity(request.capacity());
        entity.setType(request.type());
    }
}
