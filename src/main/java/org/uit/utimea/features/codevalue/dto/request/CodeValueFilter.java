package org.uit.utimea.features.codevalue.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.uit.utimea.shared.dto.request.BaseFilter;

@Data
@EqualsAndHashCode(callSuper = true)
public class CodeValueFilter extends BaseFilter {
    private Long codeId;
    private String codeValue;
    private String description;
}
