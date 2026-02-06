package org.uit.utimea.features.teacher.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.uit.utimea.features.teacher.dto.excel.TeacherExcelDTO;
import org.uit.utimea.features.teacher.dto.request.TeacherFilter;
import org.uit.utimea.features.teacher.dto.request.TeacherRequest;
import org.uit.utimea.features.teacher.dto.response.TeacherResponse;
import org.uit.utimea.features.teacher.mapper.TeacherMapper;
import org.uit.utimea.shared.config.ExcelConfig;
import org.uit.utimea.shared.dto.request.PageAndFilterDTO;
import org.uit.utimea.shared.excel.AbstractExcelService;
import org.uit.utimea.shared.excel.ExcelValidationError;
import org.uit.utimea.shared.repository.CodeValueRepository;
import org.uit.utimea.shared.entity.CodeValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Excel service implementation for Teachers.
 * Handles template generation, export, and import with multithreading support.
 */
@Slf4j
@Service
public class TeacherExcelService extends AbstractExcelService<TeacherRequest, TeacherResponse, TeacherExcelDTO> {

    private final TeacherService teacherService;
    private final CodeValueRepository codeValueRepository;
    private final TeacherMapper teacherMapper;

    public TeacherExcelService(
            TeacherService teacherService,
            CodeValueRepository codeValueRepository,
            TeacherMapper teacherMapper,
            @org.springframework.beans.factory.annotation.Qualifier("excelThreadPool") ExecutorService excelThreadPool,
            ExcelConfig excelConfig) {
        super(excelThreadPool, excelConfig.excelBatchSize());
        this.teacherService = teacherService;
        this.codeValueRepository = codeValueRepository;
        this.teacherMapper = teacherMapper;
    }

    @Override
    protected List<String> getColumnHeaders() {
        return List.of("Name", "Phone Number", "Degree", "Department Name");
    }

    @Override
    protected List<Integer> getColumnWidths() {
        return List.of(30, 20, 20, 30);
    }

    @Override
    protected String getSheetName() {
        return "Teachers";
    }

    @Override
    protected List<TeacherExcelDTO> readExcelData(Workbook workbook) throws IOException {
        List<TeacherExcelDTO> teachers = new ArrayList<>();
        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            TeacherExcelDTO teacher = new TeacherExcelDTO();

            Cell nameCell = row.getCell(0);
            if (nameCell != null) {
                teacher.setName(getCellValueAsString(nameCell));
            }

            Cell phoneCell = row.getCell(1);
            if (phoneCell != null) {
                teacher.setPhoneNumber(getCellValueAsString(phoneCell));
            }

            Cell degreeCell = row.getCell(2);
            if (degreeCell != null) {
                teacher.setDegree(getCellValueAsString(degreeCell));
            }

            Cell deptCell = row.getCell(3);
            if (deptCell != null) {
                teacher.setDepartmentName(getCellValueAsString(deptCell));
            }

            if (teacher.getName() != null && !teacher.getName().trim().isEmpty()) {
                teachers.add(teacher);
            }
        }
        
        return teachers;
    }

    @Override
    protected void writeExcelData(Workbook workbook, List<TeacherExcelDTO> data) {
        Sheet sheet = workbook.getSheet(getSheetName());
        CellStyle dataStyle = createDataStyle(workbook);
        
        int rowNum = 1;
        for (TeacherExcelDTO teacher : data) {
            Row row = sheet.createRow(rowNum++);
            
            createCell(row, 0, teacher.getName(), dataStyle);
            createCell(row, 1, teacher.getPhoneNumber(), dataStyle);
            createCell(row, 2, teacher.getDegree(), dataStyle);
            createCell(row, 3, teacher.getDepartmentName(), dataStyle);
        }
    }

    @Override
    protected List<TeacherResponse> fetchAllData() {
        PageAndFilterDTO<TeacherFilter> pageAndFilter = new PageAndFilterDTO<>();
        pageAndFilter.setPage(0);
        pageAndFilter.setSize(10000);
        var pagination = teacherService.getAll(pageAndFilter);
        return pagination.content();
    }

    @Override
    protected void createEntity(TeacherRequest request) {
        teacherService.create(request);
    }

    @Override
    public List<ExcelValidationError> validateExcelData(List<TeacherExcelDTO> excelData) {
        List<ExcelValidationError> errors = new ArrayList<>();
        
        for (int i = 0; i < excelData.size(); i++) {
            TeacherExcelDTO teacher = excelData.get(i);
            int rowNumber = i + 2;

            if (teacher.getName() == null || teacher.getName().trim().isEmpty()) {
                errors.add(ExcelValidationError.builder()
                        .rowNumber(rowNumber)
                        .column("Name")
                        .message("Name is required")
                        .build());
            }

            if (teacher.getDepartmentName() != null && !teacher.getDepartmentName().trim().isEmpty()) {
                CodeValue department = codeValueRepository.findByName(teacher.getDepartmentName())
                        .stream()
                        .findFirst()
                        .orElse(null);
                if (department == null) {
                    errors.add(ExcelValidationError.builder()
                            .rowNumber(rowNumber)
                            .column("Department Name")
                            .message("Department '" + teacher.getDepartmentName() + "' not found")
                            .invalidValue(teacher.getDepartmentName())
                            .build());
                }
            }
        }
        
        return errors;
    }

    @Override
    public TeacherRequest convertExcelDtoToRequest(TeacherExcelDTO excelDto) {
        Long departmentId = null;
        
        if (excelDto.getDepartmentName() != null && !excelDto.getDepartmentName().trim().isEmpty()) {
            departmentId = codeValueRepository.findByName(excelDto.getDepartmentName())
                    .stream()
                    .findFirst()
                    .map(CodeValue::getId)
                    .orElse(null);
        }
        
        return new TeacherRequest(
                excelDto.getName(),
                excelDto.getPhoneNumber(),
                excelDto.getDegree(),
                departmentId
        );
    }

    @Override
    public TeacherExcelDTO convertResponseToExcelDto(TeacherResponse response) {
        return TeacherExcelDTO.builder()
                .name(response.name())
                .phoneNumber(response.phoneNumber())
                .degree(response.degree())
                .departmentName(response.department() != null ? response.department().name() : null)
                .build();
    }

    // Helper methods
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                } else {
                    yield String.valueOf((long) cell.getNumericCellValue());
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private void createCell(Row row, int columnIndex, String value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        if (value != null) {
            cell.setCellValue(value);
        }
        cell.setCellStyle(style);
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
}
