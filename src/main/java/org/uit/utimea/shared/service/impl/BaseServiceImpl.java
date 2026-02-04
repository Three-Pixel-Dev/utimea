package org.uit.utimea.shared.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;
import org.uit.utimea.shared.dto.request.PageAndFilterDTO;
import org.uit.utimea.shared.dto.response.PaginationDTO;
import org.uit.utimea.shared.repository.specification.GenericSpecification;
import org.uit.utimea.shared.service.BaseService;
import org.uit.utimea.shared.util.PaginationHelper;
import org.uit.utimea.shared.repository.BaseRepository;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class BaseServiceImpl<ENTITY, REQUEST, RESPONSE, FILTER> implements BaseService<REQUEST, RESPONSE, FILTER> {

    protected final BaseRepository<ENTITY> repository;
    protected final JpaSpecificationExecutor<ENTITY> specificationExecutor;
    private final GenericSpecification<ENTITY> genericSpecification = new GenericSpecification<>();

    protected BaseServiceImpl(BaseRepository<ENTITY> repository) {
        this.repository = repository;
        this.specificationExecutor = repository;
    }

    @Override
    @Transactional
    public RESPONSE create(REQUEST request) {
        ENTITY entity = mapRequestToEntity(request);
        ENTITY savedEntity = repository.save(entity);
        return mapEntityToResponse(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public RESPONSE findById(Long id) {
        ENTITY entity = findByIdOrThrow(id);
        return mapEntityToResponse(entity);
    }

    @Override
    @Transactional
    public RESPONSE update(Long id, REQUEST request) {
        ENTITY entity = findByIdOrThrow(id);
        updateEntityFromRequest(entity, request);
        ENTITY updatedEntity = repository.save(entity);
        return mapEntityToResponse(updatedEntity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationDTO<RESPONSE> getAll(PageAndFilterDTO<FILTER> pageAndFilterDTO) {
        if (pageAndFilterDTO.getSortBy() != null && !pageAndFilterDTO.getSortBy().isEmpty()) {
            validateSortBy(pageAndFilterDTO.getSortBy());
        }
        
        FILTER filter = pageAndFilterDTO.getFilter();
        Map<String, Object> keywordMap = toMap(filter, getFieldMapping());
        
        List<String> fields = new ArrayList<>(keywordMap.keySet());
        Specification<ENTITY> spec = genericSpecification.getSpecification(keywordMap, fields);
        
        Pageable pageable = buildPageable(pageAndFilterDTO);
        
        Page<ENTITY> page;
        if (spec != null) {
            page = specificationExecutor.findAll(spec, pageable);
        } else {
            page = repository.findAll(pageable);
        }
        
        List<RESPONSE> content = page.getContent().stream()
                .map(this::mapEntityToResponse)
                .toList();
        
        return PaginationHelper.getResponse(page, content);
    }

    private Pageable buildPageable(PageAndFilterDTO<FILTER> pageAndFilterDTO) {
        Sort sort = pageAndFilterDTO.getSortBy() != null && !pageAndFilterDTO.getSortBy().isEmpty()
                ? Sort.by(Sort.Direction.fromString(pageAndFilterDTO.getSortDirection()), pageAndFilterDTO.getSortBy())
                : Sort.unsorted();
        return PageRequest.of(pageAndFilterDTO.getPage(), pageAndFilterDTO.getSize(), sort);
    }

    protected abstract ENTITY mapRequestToEntity(REQUEST request);

    protected abstract RESPONSE mapEntityToResponse(ENTITY entity);

    protected abstract void updateEntityFromRequest(ENTITY entity, REQUEST request);

    /**
     * Override this method to provide field mapping for nested properties.
     * Example: Map.of("profileId", "profileId.profileId", "skillSubcategoryId", "skillSubcategoryId.skillSubcategoryId")
     */
    protected Map<String, String> getFieldMapping() {
        return Map.of();
    }

    /**
     * Override this method to provide custom validation for sortBy field.
     * Default implementation does nothing - JPA will handle invalid sort fields.
     * Override in subclasses to validate against response DTO fields if needed.
     */
    protected void validateSortBy(String sortBy) {
        // Default implementation - can be overridden in subclasses for custom validation
        // JPA will throw an exception if the sort field is invalid
    }

    private ENTITY findByIdOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found with id: " + id));
    }

    private Map<String, Object> toMap(FILTER filter, Map<String, String> fieldMapping) {
        if (filter == null) {
            return Map.of();
        }

        Map<String, Object> map = new java.util.HashMap<>();
        Field[] fields = filter.getClass().getDeclaredFields();

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(filter);
                if (value != null) {
                    String fieldName = field.getName();
                    // Use mapped field name if available, otherwise use original field name
                    String mappedField = fieldMapping.getOrDefault(fieldName, fieldName);
                    map.put(mappedField, value);
                }
            } catch (IllegalAccessException e) {
                // Skip fields that can't be accessed
            }
        }

        return map;
    }
}
