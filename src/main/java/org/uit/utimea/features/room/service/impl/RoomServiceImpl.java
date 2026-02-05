package org.uit.utimea.features.room.service.impl;

import org.springframework.stereotype.Service;
import org.uit.utimea.shared.service.impl.BaseServiceImpl;
import org.uit.utimea.shared.entity.Room;
import org.uit.utimea.features.room.dto.request.RoomFilter;
import org.uit.utimea.features.room.dto.request.RoomRequest;
import org.uit.utimea.features.room.dto.response.RoomResponse;
import org.uit.utimea.features.room.mapper.RoomMapper;
import org.uit.utimea.features.room.service.RoomService;
import org.uit.utimea.shared.repository.RoomRepository;

@Service
public class RoomServiceImpl extends BaseServiceImpl<Room, RoomRequest, RoomResponse, RoomFilter> implements RoomService {

    private final RoomMapper roomMapper;

    public RoomServiceImpl(RoomRepository roomRepository, RoomMapper roomMapper) {
        super(roomRepository);
        this.roomMapper = roomMapper;
    }

    @Override
    protected Room mapRequestToEntity(RoomRequest request) {
        return roomMapper.toEntity(request);
    }

    @Override
    protected RoomResponse mapEntityToResponse(Room entity) {
        return roomMapper.toResponse(entity);
    }

    @Override
    protected void updateEntityFromRequest(Room entity, RoomRequest request) {
        roomMapper.updateEntity(entity, request);
    }
}
