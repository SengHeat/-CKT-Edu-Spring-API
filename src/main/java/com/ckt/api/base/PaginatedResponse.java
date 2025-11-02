package com.ckt.api.base;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import java.util.List;

@Getter
@Setter
public class PaginatedResponse<T> {
    private List<T> data;
    private Meta meta;

    public PaginatedResponse(Page<T> page) {
        this.data = page.getContent();
        this.meta = new Meta(
            page.getNumber() + 1,
            page.getSize(),
            page.getTotalPages(),
            page.getTotalElements(),
            page.hasNext(),
            page.hasPrevious()
        );
    }
}