package org.uit.utimea.features.room.service;

import org.uit.utimea.shared.service.BaseService;
import org.uit.utimea.features.room.dto.request.RoomFilter;
import org.uit.utimea.features.room.dto.request.RoomRequest;
import org.uit.utimea.features.room.dto.response.RoomResponse;

public interface RoomService extends BaseService<RoomRequest, RoomResponse, RoomFilter> {
}
