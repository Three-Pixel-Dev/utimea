package org.uit.utimea.features.teacher.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import org.uit.utimea.shared.repository.CodeRepository;
import org.uit.utimea.shared.entity.CodeValue;
import org.uit.utimea.shared.entity.Code;
import org.uit.utimea.shared.repository.UserRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * Excel service implementation for Teachers.
 * Handles template generation, export, and import with multithreading support.
 */
@Slf4j
@Service
public class TeacherExcelService extends AbstractExcelService<TeacherRequest, TeacherResponse, TeacherExcelDTO> {

    private final TeacherService teacherService;
    private final CodeValueRepository codeValueRepository;
    private final CodeRepository codeRepository;
    private final TeacherMapper teacherMapper;
    private final UserRepository userRepository;

    public TeacherExcelService(
            TeacherService teacherService,
            CodeValueRepository codeValueRepository,
            CodeRepository codeRepository,
            TeacherMapper teacherMapper,
            UserRepository userRepository,
            @org.springframework.beans.factory.annotation.Qualifier("excelThreadPool") ExecutorService excelThreadPool,
            ExcelConfig excelConfig) {
        super(excelThreadPool, excelConfig.excelBatchSize());
        this.teacherService = teacherService;
        this.codeValueRepository = codeValueRepository;
        this.codeRepository = codeRepository;
        this.teacherMapper = teacherMapper;
        this.userRepository = userRepository;
    }

    @Override
    protected List<String> getColumnHeaders() {
        return List.of("Name", "Phone Number", "Email", "Degree", "Department Name");
    }

    @Override
    protected List<Integer> getColumnWidths() {
        return List.of(30, 20, 30, 20, 30);
    }

    @Override
    protected String getSheetName() {
        return "Teachers";
    }

    @Override
    public InputStream generateTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(getSheetName());

            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(workbook);
            List<String> headers = getColumnHeaders();
            List<Integer> widths = getColumnWidths();
            
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, widths.get(i) * 256);
            }

            addDepartmentDropdownValidation((XSSFSheet) sheet);

            sheet.createFreezePane(0, 1);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
            
        } catch (IOException e) {
            log.error("Error generating Excel template", e);
            throw new RuntimeException("Failed to generate Excel template", e);
        }
    }

    private void addDepartmentDropdownValidation(XSSFSheet sheet) {
        addDepartmentDropdownValidation(sheet, 1, 10000);
    }

    private void addDepartmentDropdownValidation(XSSFSheet sheet, int firstRow, int lastRow) {
        try {
            Code departmentCode = codeRepository.findByConstantValue("DEPARTMENT")
                    .orElseThrow(() -> new RuntimeException("DEPARTMENT code not found"));
            
            List<CodeValue> departments = codeValueRepository.findByCode(departmentCode);
            List<String> departmentNames = departments.stream()
                    .sorted((d1, d2) -> Long.compare(d1.getId(), d2.getId()))
                    .map(CodeValue::getName)
                    .collect(Collectors.toList());
            
            if (departmentNames.isEmpty()) {
                log.warn("No department code values found for dropdown validation");
                return;
            }
            
            XSSFDataValidationHelper validationHelper = new XSSFDataValidationHelper(sheet);
            DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(
                    departmentNames.toArray(new String[0])
            );
            
            CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, 4, 4);
            DataValidation validation = validationHelper.createValidation(constraint, addressList);
            
            validation.setShowErrorBox(true);
            validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            validation.createErrorBox("Invalid Department", "Please select a valid department from the dropdown list.");
            validation.setShowPromptBox(true);
            validation.createPromptBox("Department", "Please select a department from the dropdown list.");
            
            sheet.addValidationData(validation);
            log.info("Added department dropdown validation with {} options for rows {}-{}", 
                    departmentNames.size(), firstRow + 1, lastRow + 1);
        } catch (Exception e) {
            log.error("Error adding department dropdown validation", e);
        }
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

            Cell emailCell = row.getCell(2);
            if (emailCell != null) {
                teacher.setEmail(getCellValueAsString(emailCell));
            }

            Cell degreeCell = row.getCell(3);
            if (degreeCell != null) {
                teacher.setDegree(getCellValueAsString(degreeCell));
            }

            Cell deptCell = row.getCell(4);
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
            createCell(row, 2, teacher.getEmail(), dataStyle);
            createCell(row, 3, teacher.getDegree(), dataStyle);
            createCell(row, 4, teacher.getDepartmentName(), dataStyle);
        }
        
        if (sheet instanceof XSSFSheet) {
            int lastDataRow = rowNum > 1 ? rowNum - 1 : 1;
            addDepartmentDropdownValidation((XSSFSheet) sheet, 1, Math.max(lastDataRow, 1000));
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
        
        // Track emails within the Excel file to detect duplicates
        java.util.Set<String> emailsInFile = new java.util.HashSet<>();
        
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

            // Validate phone number is required and contains only digits
            if (teacher.getPhoneNumber() == null || teacher.getPhoneNumber().trim().isEmpty()) {
                errors.add(ExcelValidationError.builder()
                        .rowNumber(rowNumber)
                        .column("Phone Number")
                        .message("Phone number is required")
                        .build());
            } else {
                String phoneNumber = teacher.getPhoneNumber().trim();
                if (!phoneNumber.matches("^[0-9]+$")) {
                    errors.add(ExcelValidationError.builder()
                            .rowNumber(rowNumber)
                            .column("Phone Number")
                            .message("Phone number must contain only digits")
                            .invalidValue(phoneNumber)
                            .build());
                }
            }

            if (teacher.getEmail() == null || teacher.getEmail().trim().isEmpty()) {
                errors.add(ExcelValidationError.builder()
                        .rowNumber(rowNumber)
                        .column("Email")
                        .message("Email is required")
                        .build());
            } else {
                String email = teacher.getEmail().trim();
                
                // Validate email format
                if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    errors.add(ExcelValidationError.builder()
                            .rowNumber(rowNumber)
                            .column("Email")
                            .message("Invalid email format")
                            .invalidValue(email)
                            .build());
                } else {
                    // Check for duplicate email within the same Excel file
                    if (emailsInFile.contains(email.toLowerCase())) {
                        errors.add(ExcelValidationError.builder()
                                .rowNumber(rowNumber)
                                .column("Email")
                                .message("Email '" + email + "' is duplicated in this file")
                                .invalidValue(email)
                                .build());
                    } else {
                        emailsInFile.add(email.toLowerCase());
                        
                        // Check if email already exists in the database
                        if (userRepository.findByEmail(email).isPresent()) {
                            errors.add(ExcelValidationError.builder()
                                    .rowNumber(rowNumber)
                                    .column("Email")
                                    .message("Email '" + email + "' already exists in the system")
                                    .invalidValue(email)
                                    .build());
                        }
                    }
                }
            }

            // Validate department is required
            if (teacher.getDepartmentName() == null || teacher.getDepartmentName().trim().isEmpty()) {
                errors.add(ExcelValidationError.builder()
                        .rowNumber(rowNumber)
                        .column("Department Name")
                        .message("Department is required")
                        .build());
            } else {
                Code departmentCode = codeRepository.findByConstantValue("DEPARTMENT")
                        .orElse(null);
                if (departmentCode == null) {
                    errors.add(ExcelValidationError.builder()
                            .rowNumber(rowNumber)
                            .column("Department Name")
                            .message("DEPARTMENT code not found in system")
                            .invalidValue(teacher.getDepartmentName())
                            .build());
                } else {
                    CodeValue department = codeValueRepository.findByCodeAndName(departmentCode, teacher.getDepartmentName())
                            .orElse(null);
                    if (department == null) {
                        errors.add(ExcelValidationError.builder()
                                .rowNumber(rowNumber)
                                .column("Department Name")
                                .message("Department '" + teacher.getDepartmentName() + "' not found. Please select from the dropdown list.")
                                .invalidValue(teacher.getDepartmentName())
                                .build());
                    }
                }
            }

            // Validate degree is required
            if (teacher.getDegree() == null || teacher.getDegree().trim().isEmpty()) {
                errors.add(ExcelValidationError.builder()
                        .rowNumber(rowNumber)
                        .column("Degree")
                        .message("Degree is required")
                        .build());
            }
        }
        
        return errors;
    }

    @Override
    public TeacherRequest convertExcelDtoToRequest(TeacherExcelDTO excelDto) {
        Long departmentId = null;
        
        if (excelDto.getDepartmentName() != null && !excelDto.getDepartmentName().trim().isEmpty()) {
            Code departmentCode = codeRepository.findByConstantValue("DEPARTMENT")
                    .orElse(null);
            if (departmentCode != null) {
                departmentId = codeValueRepository.findByCodeAndName(departmentCode, excelDto.getDepartmentName())
                        .map(CodeValue::getId)
                        .orElse(null);
            }
        }
        
        return new TeacherRequest(
                excelDto.getName(),
                excelDto.getPhoneNumber(),
                excelDto.getEmail(),
                excelDto.getDegree(),
                departmentId
        );
    }

    @Override
    public TeacherExcelDTO convertResponseToExcelDto(TeacherResponse response) {
        return TeacherExcelDTO.builder()
                .name(response.name())
                .phoneNumber(response.phoneNumber())
                .email(response.email())
                .degree(response.degree())
                .departmentName(response.department() != null ? response.department().name() : null)
                .build();
    }

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
