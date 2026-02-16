package org.uit.utimea.features.student.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.uit.utimea.shared.dto.request.PageAndFilterDTO;
import org.uit.utimea.shared.dto.response.PaginationDTO;
import org.uit.utimea.shared.entity.Profile;
import org.uit.utimea.features.student.dto.request.StudentFilter;
import org.uit.utimea.features.student.dto.request.StudentRequest;
import org.uit.utimea.features.student.dto.response.StudentResponse;
import org.uit.utimea.features.student.mapper.StudentMapper;
import org.uit.utimea.features.student.service.StudentService;
import org.uit.utimea.shared.repository.ProfileRepository;
import org.uit.utimea.shared.repository.specification.GenericSpecification;
import org.uit.utimea.shared.service.impl.BaseServiceImpl;
import org.uit.utimea.shared.util.PaginationHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudentServiceImpl extends BaseServiceImpl<Profile, StudentRequest, StudentResponse, StudentFilter> implements StudentService {

    private final StudentMapper studentMapper;

    public StudentServiceImpl(ProfileRepository profileRepository, StudentMapper studentMapper) {
        super(profileRepository);
        this.studentMapper = studentMapper;
    }

    @Override
    protected Profile mapRequestToEntity(StudentRequest request) {
        validateStudentRequest(request);
        return studentMapper.toEntity(request);
    }

    @Override
    protected StudentResponse mapEntityToResponse(Profile entity) {
        return studentMapper.toResponse(entity);
    }

    @Override
    protected void updateEntityFromRequest(Profile entity, StudentRequest request) {
        validateStudentRequest(request);
        studentMapper.updateEntity(entity, request);
    }

    @Override
    protected Map<String, String> getFieldMapping() {
        return Map.of(
                "batchId", "batch.id",
                "majorSectionId", "majorSection.id"
        );
    }

    @Override
    public StudentResponse findById(Long id) {
        Profile entity = ((ProfileRepository) repository).findByIdWithUser(id)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found with id: " + id));
        // Verify it's a student (has batch or majorSection)
        if (entity.getBatch() == null && entity.getMajorSection() == null) {
            throw new EntityNotFoundException("Profile with id " + id + " is not a student");
        }
        return mapEntityToResponse(entity);
    }

    @Override
    public StudentResponse update(Long id, StudentRequest request) {
        Profile entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found with id: " + id));
        // Verify it's a student
        if (entity.getBatch() == null && entity.getMajorSection() == null) {
            throw new EntityNotFoundException("Profile with id " + id + " is not a student");
        }
        updateEntityFromRequest(entity, request);
        Profile updatedEntity = repository.save(entity);
        return mapEntityToResponse(updatedEntity);
    }

    @Override
    public void delete(Long id) {
        Profile entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found with id: " + id));
        // Verify it's a student
        if (entity.getBatch() == null && entity.getMajorSection() == null) {
            throw new EntityNotFoundException("Profile with id " + id + " is not a student");
        }
        repository.deleteById(id);
    }

    @Override
    public PaginationDTO<StudentResponse> getAll(PageAndFilterDTO<StudentFilter> pageAndFilterDTO) {
        // Build base specification using parent's logic
        if (pageAndFilterDTO.getSortBy() != null && !pageAndFilterDTO.getSortBy().isEmpty()) {
            validateSortBy(pageAndFilterDTO.getSortBy());
        }
        
        StudentFilter filter = pageAndFilterDTO.getFilter();
        Map<String, Object> keywordMap = toMapUsingReflection(filter, getFieldMapping());
        
        List<String> fields = new ArrayList<>(keywordMap.keySet());
        GenericSpecification<Profile> genericSpec = new GenericSpecification<>();
        Specification<Profile> baseSpec = genericSpec.getSpecification(keywordMap, fields);
        
        // Add student filter (must have batch or majorSection)
        Specification<Profile> studentSpec = (root, query, cb) -> {
            if (!query.getResultType().equals(Long.class)) {
                root.fetch("user", jakarta.persistence.criteria.JoinType.LEFT);
            }
            Predicate hasBatch = cb.isNotNull(root.get("batch"));
            Predicate hasMajorSection = cb.isNotNull(root.get("majorSection"));
            return cb.or(hasBatch, hasMajorSection);
        };
        
        // Combine specifications
        Specification<Profile> finalSpec = baseSpec != null 
                ? baseSpec.and(studentSpec) 
                : studentSpec;
        
        Pageable pageable = buildPageable(pageAndFilterDTO);
        Page<Profile> page = specificationExecutor.findAll(finalSpec, pageable);
        
        List<StudentResponse> content = page.getContent().stream()
                .map(this::mapEntityToResponse)
                .toList();
        
        return PaginationHelper.getResponse(page, content);
    }

    private Pageable buildPageable(PageAndFilterDTO<StudentFilter> pageAndFilterDTO) {
        Sort sort = pageAndFilterDTO.getSortBy() != null && !pageAndFilterDTO.getSortBy().isEmpty()
                ? Sort.by(Sort.Direction.fromString(pageAndFilterDTO.getSortDirection()), pageAndFilterDTO.getSortBy())
                : Sort.unsorted();
        return PageRequest.of(pageAndFilterDTO.getPage(), pageAndFilterDTO.getSize(), sort);
    }

    private Map<String, Object> toMapUsingReflection(StudentFilter filter, Map<String, String> fieldMapping) {
        if (filter == null) {
            return Map.of();
        }

        Map<String, Object> map = new HashMap<>();
        
        // Handle records using record components
        if (filter.getClass().isRecord()) {
            java.lang.reflect.RecordComponent[] components = filter.getClass().getRecordComponents();
            for (java.lang.reflect.RecordComponent component : components) {
                try {
                    Object value = component.getAccessor().invoke(filter);
                    if (value != null) {
                        String fieldName = component.getName();
                        String mappedField = fieldMapping.getOrDefault(fieldName, fieldName);
                        map.put(mappedField, value);
                    }
                } catch (Exception e) {
                    // Skip components that can't be accessed
                }
            }
        } else {
            // Handle regular classes
            Field[] fields = filter.getClass().getDeclaredFields();
            for (Field field : fields) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(filter);
                    if (value != null) {
                        String fieldName = field.getName();
                        String mappedField = fieldMapping.getOrDefault(fieldName, fieldName);
                        map.put(mappedField, value);
                    }
                } catch (IllegalAccessException e) {
                    // Skip fields that can't be accessed
                }
            }
        }

        return map;
    }

    private void validateStudentRequest(StudentRequest request) {
        // Validate phone number is required and contains only digits
        if (request.phoneNumber() == null || request.phoneNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required");
        }
        String phoneNumber = request.phoneNumber().trim();
        if (!phoneNumber.matches("^[0-9]+$")) {
            throw new IllegalArgumentException("Phone number must contain only digits");
        }

        // Validate email is required
        if (request.email() == null || request.email().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        // Validate batch is required
        if (request.batchId() == null) {
            throw new IllegalArgumentException("Batch is required");
        }

        // Validate major section is required
        if (request.majorSectionId() == null) {
            throw new IllegalArgumentException("Major Section is required");
        }
    }
}
