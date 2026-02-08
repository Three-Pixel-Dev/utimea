package org.uit.utimea.features.room.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.uit.utimea.shared.dto.request.BaseFilter;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoomFilter extends BaseFilter {
    private String name;
    private Integer capacity;
    private Long roomTypeId;
}
