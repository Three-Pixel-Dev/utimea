package org.uit.utimea.features.timetable.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TimetableResponseDto {
    private Long id;
    private String day;
    private String period;
    private String subject;
    private String room;
    private String label;
}
