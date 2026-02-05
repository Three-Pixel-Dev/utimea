package org.uit.utimea.features.subject.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.uit.utimea.shared.dto.request.BaseFilter;

@Data
@EqualsAndHashCode(callSuper = true)
public class SubjectFilter extends BaseFilter {
    private String code;
    private String description;
}
