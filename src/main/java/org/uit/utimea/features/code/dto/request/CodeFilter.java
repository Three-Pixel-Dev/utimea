package org.uit.utimea.features.code.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.uit.utimea.shared.dto.request.BaseFilter;

@Data
@EqualsAndHashCode(callSuper = true)
public class CodeFilter extends BaseFilter {
    private String name;
    private String constantValue;
}
