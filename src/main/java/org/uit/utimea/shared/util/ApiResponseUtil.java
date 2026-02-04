package org.uit.utimea.shared.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.uit.utimea.shared.dto.response.ApiMetaResponse;
import org.uit.utimea.shared.dto.response.ApiResponse;
import org.uit.utimea.shared.dto.response.PaginationDTO;

public class ApiResponseUtil {

    public static ApiResponse success(Object data, String message, HttpServletRequest request) {
        ApiMetaResponse meta = ApiMetaResponse.builder()
                .endpoint(request.getRequestURI())
                .method(request.getMethod())
                .totalItems(0)
                .totalPages(0)
                .currentPage(0)
                .build();

        return ApiResponse.builder()
                .success(1)
                .code(HttpStatus.OK.value())
                .meta(meta)
                .data(data)
                .message(message)
                .build();
    }

    public static ApiResponse created(Object data, String message, HttpServletRequest request) {
        ApiMetaResponse meta = ApiMetaResponse.builder()
                .endpoint(request.getRequestURI())
                .method(request.getMethod())
                .totalItems(0)
                .totalPages(0)
                .currentPage(0)
                .build();

        return ApiResponse.builder()
                .success(1)
                .code(HttpStatus.CREATED.value())
                .meta(meta)
                .data(data)
                .message(message)
                .build();
    }

    public static ApiResponse noContent(String message, HttpServletRequest request) {
        ApiMetaResponse meta = ApiMetaResponse.builder()
                .endpoint(request.getRequestURI())
                .method(request.getMethod())
                .totalItems(0)
                .totalPages(0)
                .currentPage(0)
                .build();

        return ApiResponse.builder()
                .success(1)
                .code(HttpStatus.NO_CONTENT.value())
                .meta(meta)
                .data(null)
                .message(message)
                .build();
    }

    public static <T> ApiResponse paginated(PaginationDTO<T> pagination, String message, HttpServletRequest request) {
        ApiMetaResponse meta = ApiMetaResponse.builder()
                .endpoint(request.getRequestURI())
                .method(request.getMethod())
                .totalItems(pagination.totalItems())
                .totalPages(pagination.totalPages())
                .currentPage(pagination.currentPage())
                .build();

        return ApiResponse.builder()
                .success(1)
                .code(HttpStatus.OK.value())
                .meta(meta)
                .data(pagination)
                .message(message)
                .build();
    }

    public static ApiResponse error(String message, HttpStatus status, HttpServletRequest request) {
        ApiMetaResponse meta = ApiMetaResponse.builder()
                .endpoint(request.getRequestURI())
                .method(request.getMethod())
                .totalItems(0)
                .totalPages(0)
                .currentPage(0)
                .build();

        return ApiResponse.builder()
                .success(0)
                .code(status.value())
                .meta(meta)
                .data(null)
                .message(message)
                .build();
    }

    public static ApiResponse error(String message, HttpStatus status, Object data, HttpServletRequest request) {
        ApiMetaResponse meta = ApiMetaResponse.builder()
                .endpoint(request.getRequestURI())
                .method(request.getMethod())
                .totalItems(0)
                .totalPages(0)
                .currentPage(0)
                .build();

        return ApiResponse.builder()
                .success(0)
                .code(status.value())
                .meta(meta)
                .data(data)
                .message(message)
                .build();
    }
}
