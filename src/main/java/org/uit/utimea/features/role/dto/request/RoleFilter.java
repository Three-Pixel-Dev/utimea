package org.uit.utimea.features.role.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.uit.utimea.shared.dto.request.BaseFilter;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleFilter extends BaseFilter {
    private String name;
}
