package org.uit.utimea.features.user.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.uit.utimea.shared.dto.request.BaseFilter;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserFilter extends BaseFilter {
    private String email;
    private Long roleId;
}
