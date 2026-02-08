package org.uit.utimea.features.codevalue.dto.request;

public record CodeValueRequest(
        Long codeId,
        String name,
        Boolean systemDefined
) {}
