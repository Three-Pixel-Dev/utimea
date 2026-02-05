package org.uit.utimea.features.majorsection.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.uit.utimea.shared.dto.request.BaseFilter;

@Data
@EqualsAndHashCode(callSuper = true)
public class MajorSectionFilter extends BaseFilter {
    private String name;
    private Long majorSectionYearId;
}
