package com.studypals.global.request;

import org.springframework.data.domain.Sort;

public interface SortType {
    String name();

    String getField();

    Sort.Direction getDirection();
}
