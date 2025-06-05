package com.studypals.global.resolver;

import java.util.Optional;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.studypals.global.annotations.CursorDefault;
import com.studypals.global.request.Cursor;
import com.studypals.global.request.SortOrder;
import com.studypals.global.request.SortType;

public class CursorDefaultResolver implements HandlerMethodArgumentResolver {
    private static final String CURSOR_PARAM = "cursor";
    private static final String SIZE_PARAM = "size";
    private static final String SORT_PARAM = "sort";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType() == Cursor.class && parameter.hasParameterAnnotation(CursorDefault.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory)
            throws Exception {
        CursorDefault annotation = getCursorDefault(parameter);
        long cursor = getCursor(webRequest, annotation);
        int size = getSize(webRequest, annotation);
        SortOrder sort = getSort(webRequest, annotation);
        return new Cursor(cursor, size, sort);
    }

    private CursorDefault getCursorDefault(MethodParameter parameter) {
        return Optional.ofNullable(parameter.getParameterAnnotation(CursorDefault.class))
                .orElseThrow(() -> new IllegalArgumentException("@CursorDefault annotation is required"));
    }

    private long getCursor(NativeWebRequest webRequest, CursorDefault cursorDefault) {
        try {
            String cursorParam = Optional.ofNullable(webRequest.getParameter(CURSOR_PARAM))
                    .orElse(String.valueOf(cursorDefault.cursor()));
            return Long.parseLong(cursorParam);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid cursor parameter", e);
        }
    }

    private int getSize(NativeWebRequest webRequest, CursorDefault cursorDefault) {
        try {
            String sizeParam = Optional.ofNullable(webRequest.getParameter(SIZE_PARAM))
                    .orElse(String.valueOf(cursorDefault.size()));
            return Integer.parseInt(sizeParam);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid cursor parameter", e);
        }
    }

    private SortOrder getSort(NativeWebRequest webRequest, CursorDefault cursorDefault) {
        String sortParam =
                Optional.ofNullable(webRequest.getParameter(SORT_PARAM)).orElse(cursorDefault.sort());
        SortType type = SortTypeResolver.resolve(sortParam)
                .orElseThrow(() -> new IllegalArgumentException("Invalid sort parameter"));
        return new SortOrder(type.getField(), type.getDirection());
    }
}
