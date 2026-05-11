package io.antcamp.notificationservice.domain.repository;

import java.util.List;

public record PageResult<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int currentPage
) {}