package org.uit.utimea.features.student.dto.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Student Excel import/export operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentExcelDTO {
    
    private String name;
    private String phoneNumber;
    private String email;
    private String batchName;
    private String majorSectionName;
}
