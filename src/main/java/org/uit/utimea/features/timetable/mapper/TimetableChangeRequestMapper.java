package org.uit.utimea.features.timetable.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.uit.utimea.features.timetable.dto.request.PeriodChangeRequest;
import org.uit.utimea.features.timetable.dto.request.RoomChangeRequest;
import org.uit.utimea.features.timetable.dto.response.TimetableChangeRequestResponse;
import org.uit.utimea.features.timetable.dto.response.TimetableDataResponse;
import org.uit.utimea.shared.dto.response.MasterData;
import org.uit.utimea.shared.entity.*;
import org.uit.utimea.shared.mapper.MasterDataMapper;
import org.uit.utimea.shared.repository.*;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TimetableChangeRequestMapper {

    private final MasterDataMapper masterDataMapper;
    private final TimetableDataRepository timetableDataRepository;
    private final CodeValueRepository codeValueRepository;
    private final RoomRepository roomRepository;
    private final ProfileRepository profileRepository;
    private final TimetableMapper timetableMapper;

    public TimetableChangeRequest toEntityForPeriodChange(PeriodChangeRequest request, Long teacherId) {
        TimetableData timetableData = timetableDataRepository.findById(request.timetableDataId())
                .orElseThrow(() -> new RuntimeException("TimetableData not found with id: " + request.timetableDataId()));

        CodeValue newDay = codeValueRepository.findById(request.newTimetableDayId())
                .orElseThrow(() -> new RuntimeException("Timetable day not found with id: " + request.newTimetableDayId()));

        CodeValue newPeriod = codeValueRepository.findById(request.newTimetablePeriodId())
                .orElseThrow(() -> new RuntimeException("Timetable period not found with id: " + request.newTimetablePeriodId()));

        Profile teacher = profileRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + teacherId));

        TimetableChangeRequest.RequestScope scope = TimetableChangeRequest.RequestScope.valueOf(request.requestScope());

        return TimetableChangeRequest.builder()
                .timetableData(timetableData)
                .requestType(TimetableChangeRequest.RequestType.PERIOD_CHANGE)
                .requestScope(scope)
                .status(TimetableChangeRequest.RequestStatus.PENDING)
                .newTimetableDay(newDay)
                .newTimetablePeriod(newPeriod)
                .specificDate(request.specificDate())
                .requestedBy(teacher)
                .requestReason(request.requestReason())
                .requestedAt(LocalDateTime.now())
                .build();
    }

    public TimetableChangeRequest toEntityForRoomChange(RoomChangeRequest request, Long teacherId) {
        TimetableData timetableData = timetableDataRepository.findById(request.timetableDataId())
                .orElseThrow(() -> new RuntimeException("TimetableData not found with id: " + request.timetableDataId()));

        Room newRoom = roomRepository.findById(request.newRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + request.newRoomId()));

        Profile teacher = profileRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + teacherId));

        TimetableChangeRequest.RequestScope scope = TimetableChangeRequest.RequestScope.valueOf(request.requestScope());

        return TimetableChangeRequest.builder()
                .timetableData(timetableData)
                .requestType(TimetableChangeRequest.RequestType.ROOM_CHANGE)
                .requestScope(scope)
                .status(TimetableChangeRequest.RequestStatus.PENDING)
                .newRoom(newRoom)
                .specificDate(request.specificDate())
                .requestedBy(teacher)
                .requestReason(request.requestReason())
                .requestedAt(LocalDateTime.now())
                .build();
    }

    public TimetableChangeRequestResponse toResponse(TimetableChangeRequest entity) {
        if (entity == null) {
            return null;
        }

        TimetableChangeRequestResponse.RoomResponse newRoomResponse = null;
        if (entity.getNewRoom() != null) {
            newRoomResponse = TimetableChangeRequestResponse.RoomResponse.builder()
                    .id(entity.getNewRoom().getId())
                    .name(entity.getNewRoom().getName())
                    .capacity(entity.getNewRoom().getCapacity())
                    .build();
        }

        TimetableChangeRequestResponse.CodeValueResponse newDayResponse = null;
        if (entity.getNewTimetableDay() != null) {
            newDayResponse = TimetableChangeRequestResponse.CodeValueResponse.builder()
                    .id(entity.getNewTimetableDay().getId())
                    .name(entity.getNewTimetableDay().getName())
                    .build();
        }

        TimetableChangeRequestResponse.CodeValueResponse newPeriodResponse = null;
        if (entity.getNewTimetablePeriod() != null) {
            newPeriodResponse = TimetableChangeRequestResponse.CodeValueResponse.builder()
                    .id(entity.getNewTimetablePeriod().getId())
                    .name(entity.getNewTimetablePeriod().getName())
                    .build();
        }

        TimetableChangeRequestResponse.TeacherResponse teacherResponse = null;
        if (entity.getRequestedBy() != null) {
            teacherResponse = TimetableChangeRequestResponse.TeacherResponse.builder()
                    .id(entity.getRequestedBy().getId())
                    .name(entity.getRequestedBy().getName())
                    .phoneNumber(entity.getRequestedBy().getPhoneNumber())
                    .degree(entity.getRequestedBy().getDegree())
                    .build();
        }

        TimetableChangeRequestResponse.UserResponse userResponse = null;
        if (entity.getProcessedBy() != null) {
            userResponse = TimetableChangeRequestResponse.UserResponse.builder()
                    .id(entity.getProcessedBy().getId())
                    .email(entity.getProcessedBy().getEmail())
                    .build();
        }

        MasterData masterData = masterDataMapper.toMasterData(entity);

        TimetableDataResponse timetableDataResponse = timetableMapper.mapTimetableDataToResponse(entity.getTimetableData());

        return TimetableChangeRequestResponse.builder()
                .id(entity.getId())
                .timetableData(timetableDataResponse)
                .requestType(entity.getRequestType().name())
                .requestScope(entity.getRequestScope().name())
                .status(entity.getStatus().name())
                .newRoom(newRoomResponse)
                .newTimetableDay(newDayResponse)
                .newTimetablePeriod(newPeriodResponse)
                .specificDate(entity.getSpecificDate())
                .requestedBy(teacherResponse)
                .processedBy(userResponse)
                .processedAt(entity.getProcessedAt())
                .requestReason(entity.getRequestReason())
                .adminComment(entity.getAdminComment())
                .requestedAt(entity.getRequestedAt())
                .masterData(masterData)
                .build();
    }
}
