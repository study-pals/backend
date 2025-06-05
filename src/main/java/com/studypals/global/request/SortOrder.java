package com.studypals.global.request;

import org.springframework.data.domain.Sort;

public record SortOrder(String field, Sort.Direction direction) {}
