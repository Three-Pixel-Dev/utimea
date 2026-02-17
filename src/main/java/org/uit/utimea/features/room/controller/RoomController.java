package org.uit.utimea.features.room.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uit.utimea.shared.dto.request.PageAndFilterDTO;
import org.uit.utimea.shared.dto.response.ApiResponse;
import org.uit.utimea.shared.util.ApiResponseUtil;
import org.uit.utimea.features.room.dto.request.RoomFilter;
import org.uit.utimea.features.room.dto.request.RoomRequest;
import org.uit.utimea.features.room.dto.response.RoomResponse;
import org.uit.utimea.features.room.service.RoomService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<ApiResponse> create(@RequestBody RoomRequest request, HttpServletRequest httpServletRequest) {
        RoomResponse response = roomService.create(request);
        ApiResponse apiResponse = ApiResponseUtil.created(
                response,
                "Room created successfully",
                httpServletRequest
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/pageable")
    public ResponseEntity<ApiResponse> getAll(@RequestBody(required = false) PageAndFilterDTO<RoomFilter> pageAndFilterDTO, HttpServletRequest httpServletRequest) {
        if (pageAndFilterDTO == null) {
            pageAndFilterDTO = new PageAndFilterDTO<>();
        }
        var pagination = roomService.getAll(pageAndFilterDTO);
        ApiResponse apiResponse = ApiResponseUtil.paginated(
                pagination,
                "Rooms retrieved successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> findById(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        RoomResponse response = roomService.findById(id);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "Room retrieved successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable Long id, @RequestBody RoomRequest request, HttpServletRequest httpServletRequest) {
        RoomResponse response = roomService.update(id, request);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "Room updated successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        roomService.delete(id);
        ApiResponse apiResponse = ApiResponseUtil.noContent(
                "Room deleted successfully",
                httpServletRequest
        );
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }

    @PostMapping("/bulk-delete")
    public ResponseEntity<ApiResponse> deleteMany(@RequestBody org.uit.utimea.shared.dto.request.BulkDeleteRequest request, HttpServletRequest httpServletRequest) {
        roomService.deleteMany(request.ids());
        ApiResponse apiResponse = ApiResponseUtil.noContent(
                "Rooms deleted successfully",
                httpServletRequest
        );
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }
}
