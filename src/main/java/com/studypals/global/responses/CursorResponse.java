package com.studypals.global.responses;

import java.util.List;

public record CursorResponse<T>(List<T> content, Long next, boolean hasNext) {}
