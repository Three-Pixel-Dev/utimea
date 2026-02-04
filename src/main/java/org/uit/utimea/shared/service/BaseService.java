package org.uit.utimea.shared.service;

import org.uit.utimea.shared.dto.request.PageAndFilterDTO;
import org.uit.utimea.shared.dto.response.PaginationDTO;

public interface BaseService<REQUEST, RESPONSE, FILTER> {
    RESPONSE create(REQUEST request);
    RESPONSE findById(Long id);
    RESPONSE update(Long id, REQUEST request);
    void delete(Long id);
    PaginationDTO<RESPONSE> getAll(PageAndFilterDTO<FILTER> pageAndFilterDTO);
}
