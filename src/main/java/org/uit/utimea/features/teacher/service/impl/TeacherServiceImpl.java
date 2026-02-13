package org.uit.utimea.features.teacher.service.impl;

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
import org.uit.utimea.features.teacher.dto.request.TeacherFilter;
import org.uit.utimea.features.teacher.dto.request.TeacherRequest;
import org.uit.utimea.features.teacher.dto.response.TeacherResponse;
import org.uit.utimea.features.teacher.mapper.TeacherMapper;
import org.uit.utimea.features.teacher.service.TeacherService;
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
public class TeacherServiceImpl extends BaseServiceImpl<Profile, TeacherRequest, TeacherResponse, TeacherFilter> implements TeacherService {

    private final TeacherMapper teacherMapper;

    public TeacherServiceImpl(ProfileRepository profileRepository, TeacherMapper teacherMapper) {
        super(profileRepository);
        this.teacherMapper = teacherMapper;
    }

    @Override
    protected Profile mapRequestToEntity(TeacherRequest request) {
        return teacherMapper.toEntity(request);
    }

    @Override
    protected TeacherResponse mapEntityToResponse(Profile entity) {
        return teacherMapper.toResponse(entity);
    }

    @Override
    protected void updateEntityFromRequest(Profile entity, TeacherRequest request) {
        teacherMapper.updateEntity(entity, request);
    }

    @Override
    protected Map<String, String> getFieldMapping() {
        return Map.of("departmentId", "department.id");
    }

    @Override
    public TeacherResponse findById(Long id) {
        Profile entity = ((ProfileRepository) repository).findByIdWithUser(id)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found with id: " + id));
        // Verify it's a teacher (has degree or department)
        if (entity.getDegree() == null && entity.getDepartment() == null) {
            throw new EntityNotFoundException("Profile with id " + id + " is not a teacher");
        }
        return mapEntityToResponse(entity);
    }

    @Override
    public TeacherResponse update(Long id, TeacherRequest request) {
        Profile entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found with id: " + id));
        // Verify it's a teacher
        if (entity.getDegree() == null && entity.getDepartment() == null) {
            throw new EntityNotFoundException("Profile with id " + id + " is not a teacher");
        }
        updateEntityFromRequest(entity, request);
        Profile updatedEntity = repository.save(entity);
        return mapEntityToResponse(updatedEntity);
    }

    @Override
    public void delete(Long id) {
        Profile entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found with id: " + id));
        // Verify it's a teacher
        if (entity.getDegree() == null && entity.getDepartment() == null) {
            throw new EntityNotFoundException("Profile with id " + id + " is not a teacher");
        }
        repository.deleteById(id);
    }

    @Override
    public PaginationDTO<TeacherResponse> getAll(PageAndFilterDTO<TeacherFilter> pageAndFilterDTO) {
        // Build base specification using parent's logic
        if (pageAndFilterDTO.getSortBy() != null && !pageAndFilterDTO.getSortBy().isEmpty()) {
            validateSortBy(pageAndFilterDTO.getSortBy());
        }
        
        TeacherFilter filter = pageAndFilterDTO.getFilter();
        Map<String, Object> keywordMap = toMapUsingReflection(filter, getFieldMapping());
        
        List<String> fields = new ArrayList<>(keywordMap.keySet());
        GenericSpecification<Profile> genericSpec = new GenericSpecification<>();
        Specification<Profile> baseSpec = genericSpec.getSpecification(keywordMap, fields);
        
        // Add teacher filter (must have degree or department)
        Specification<Profile> teacherSpec = (root, query, cb) -> {
            // Fetch User relationship to avoid lazy loading issues
            root.fetch("user", jakarta.persistence.criteria.JoinType.LEFT);
            Predicate hasDegree = cb.isNotNull(root.get("degree"));
            Predicate hasDepartment = cb.isNotNull(root.get("department"));
            return cb.or(hasDegree, hasDepartment);
        };
        
        // Combine specifications
        Specification<Profile> finalSpec = baseSpec != null 
                ? baseSpec.and(teacherSpec) 
                : teacherSpec;
        
        Pageable pageable = buildPageable(pageAndFilterDTO);
        Page<Profile> page = specificationExecutor.findAll(finalSpec, pageable);
        
        List<TeacherResponse> content = page.getContent().stream()
                .map(this::mapEntityToResponse)
                .toList();
        
        return PaginationHelper.getResponse(page, content);
    }

    private Pageable buildPageable(PageAndFilterDTO<TeacherFilter> pageAndFilterDTO) {
        Sort sort = pageAndFilterDTO.getSortBy() != null && !pageAndFilterDTO.getSortBy().isEmpty()
                ? Sort.by(Sort.Direction.fromString(pageAndFilterDTO.getSortDirection()), pageAndFilterDTO.getSortBy())
                : Sort.unsorted();
        return PageRequest.of(pageAndFilterDTO.getPage(), pageAndFilterDTO.getSize(), sort);
    }

    private Map<String, Object> toMapUsingReflection(TeacherFilter filter, Map<String, String> fieldMapping) {
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
}
