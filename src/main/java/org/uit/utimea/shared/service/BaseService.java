package org.uit.utimea.shared.service;

import org.uit.utimea.shared.dto.request.PageAndFilterDTO;
import org.uit.utimea.shared.dto.response.PaginationDTO;

import java.util.List;

public interface BaseService<REQUEST, RESPONSE, FILTER> {
    RESPONSE create(REQUEST request);
    RESPONSE findById(Long id);
    RESPONSE update(Long id, REQUEST request);
    void delete(Long id);
    void deleteMany(List<Long> ids);
    PaginationDTO<RESPONSE> getAll(PageAndFilterDTO<FILTER> pageAndFilterDTO);
}
