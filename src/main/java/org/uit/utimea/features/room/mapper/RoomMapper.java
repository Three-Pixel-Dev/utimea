package org.uit.utimea.features.room.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.uit.utimea.shared.mapper.MasterDataMapper;
import org.uit.utimea.shared.entity.Room;
import org.uit.utimea.shared.entity.CodeValue;
import org.uit.utimea.features.room.dto.request.RoomRequest;
import org.uit.utimea.features.room.dto.response.RoomResponse;
import org.uit.utimea.features.room.dto.response.CodeValueResponse;
import org.uit.utimea.shared.repository.CodeValueRepository;

@Component
@RequiredArgsConstructor
public class RoomMapper {

    private final MasterDataMapper masterDataMapper;
    private final CodeValueRepository codeValueRepository;

    public Room toEntity(RoomRequest request) {
        Room.RoomBuilder builder = Room.builder()
                .name(request.name())
                .capacity(request.capacity());
        
        if (request.roomTypeId() != null) {
            CodeValue roomType = codeValueRepository.findById(request.roomTypeId())
                    .orElseThrow(() -> new RuntimeException("CodeValue not found with id: " + request.roomTypeId()));
            builder.roomType(roomType);
        }
        
        return builder.build();
    }

    public RoomResponse toResponse(Room entity) {
        if (entity == null) {
            return null;
        }
        
        CodeValueResponse roomTypeResponse = null;
        if (entity.getRoomType() != null) {
            roomTypeResponse = CodeValueResponse.builder()
                    .id(entity.getRoomType().getId())
                    .name(entity.getRoomType().getName())
                    .build();
        }
        
        return RoomResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .capacity(entity.getCapacity())
                .roomType(roomTypeResponse)
                .masterData(masterDataMapper.toMasterData(entity))
                .build();
    }

    public void updateEntity(Room entity, RoomRequest request) {
        entity.setName(request.name());
        entity.setCapacity(request.capacity());
        
        if (request.roomTypeId() != null) {
            CodeValue roomType = codeValueRepository.findById(request.roomTypeId())
                    .orElseThrow(() -> new RuntimeException("CodeValue not found with id: " + request.roomTypeId()));
            entity.setRoomType(roomType);
        } else {
            entity.setRoomType(null);
        }
    }
}
