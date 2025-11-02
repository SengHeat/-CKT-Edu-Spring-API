package com.ckt.api.base;

public record Meta(
        int total,
        int perPage,
        int currentPage,
        long lastPage,
        boolean hasNextPage,
        boolean hasPreviousPage)
{}