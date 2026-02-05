package org.uit.utimea.features.profile.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.uit.utimea.shared.dto.request.BaseFilter;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProfileFilter extends BaseFilter {
    private String name;
    private String phoneNumber;
    private String degree;
    private Long departmentId;
    private Long batchId;
    private Long majorSectionId;
}
