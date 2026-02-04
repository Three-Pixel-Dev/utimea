package org.uit.utimea.shared.util;

import org.springframework.data.domain.Page;
import org.uit.utimea.shared.dto.response.PaginationDTO;

import java.util.List;

public class PaginationHelper {
    
    public static <T> PaginationDTO<T> getResponse(Page<?> page, List<T> content) {
        return PaginationDTO.<T>builder()
                .content(content)
                .totalItems((int) page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .build();
    }
}
