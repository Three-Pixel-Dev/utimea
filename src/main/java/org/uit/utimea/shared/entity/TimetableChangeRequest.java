package org.uit.utimea.shared.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "timetable_change_request")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TimetableChangeRequest extends MasterEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timetable_data_id", nullable = false)
    private TimetableData timetableData;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false)
    private RequestType requestType;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_scope", nullable = false)
    private RequestScope requestScope;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_room_id")
    private Room newRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_timetable_day_id")
    private CodeValue newTimetableDay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_timetable_period_id")
    private CodeValue newTimetablePeriod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_room_id")
    private Room originalRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_timetable_day_id")
    private CodeValue originalTimetableDay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_timetable_period_id")
    private CodeValue originalTimetablePeriod;

    @Column(name = "specific_date")
    private LocalDate specificDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    private Profile requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private User processedBy;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "request_reason", columnDefinition = "TEXT")
    private String requestReason;

    @Column(name = "admin_comment", columnDefinition = "TEXT")
    private String adminComment;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    public enum RequestType {
        ROOM_CHANGE,
        PERIOD_CHANGE
    }

    public enum RequestScope {
        SPECIFIC_DATE,
        PERMANENT
    }

    public enum RequestStatus {
        PENDING,
        APPROVED,
        DECLINED,
        COMPLETED
    }
}
