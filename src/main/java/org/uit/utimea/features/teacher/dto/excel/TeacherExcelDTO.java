package org.uit.utimea.features.teacher.dto.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Teacher Excel import/export operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherExcelDTO {
    
    private String name;
    private String phoneNumber;
    private String degree;
    private String departmentName;
}
