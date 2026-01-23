package com.studypals.global.resolver;

import java.util.Optional;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import lombok.AllArgsConstructor;

import com.studypals.global.annotations.CursorDefault;
import com.studypals.global.request.Cursor;
import com.studypals.global.request.SortType;

/**
 * 웹 요청으로 부터 {@link CursorDefault} 어노테이션이 등록된 API 메서드 파라미터를 매핑해 전달하는 resolver 입니다.
 *
 * @see HandlerMethodArgumentResolver
 * @author s0o0bn
 * @since 2025-06-05
 */
@AllArgsConstructor
@Component
public class CursorDefaultResolver implements HandlerMethodArgumentResolver {
    private static final String CURSOR_PARAM = "cursor";
    private static final String SIZE_PARAM = "size";
    private static final String SORT_PARAM = "sort";
    private static final String VALUE_PARAM = "value";

    private final SortTypeResolver sortTypeResolver;

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
        SortType sort = getSort(webRequest, annotation);
        String value = getValue(webRequest, annotation);
        return new Cursor(cursor, value, size, sort);
    }

    /**
     * 메서드에서 {@code CursorDefault} 어노테이션이 등록된 파라미터를 등록합니다.
     * 존재하지 않으면, 예외가 발생합니다.
     *
     * @param parameter {@link MethodParameter}
     * @return {@link CursorDefault}
     */
    private CursorDefault getCursorDefault(MethodParameter parameter) {
        return Optional.ofNullable(parameter.getParameterAnnotation(CursorDefault.class))
                .orElseThrow(() -> new IllegalArgumentException("@CursorDefault annotation is required"));
    }

    /**
     * Http 요청 파라미터에서 {@code cursor} 에 해당하는 값을 파싱합니다.
     * 없으면, 디폴트 값을 반환합니다.
     *
     * @param webRequest 현재 웹 요청 {@link NativeWebRequest}
     * @param cursorDefault 디폴트 값을 갖고 있는 {@code CursorDefault} annotation
     * @return 페이징 시 조회 시작 기준이 될 커서 값
     */
    private long getCursor(NativeWebRequest webRequest, CursorDefault cursorDefault) {
        try {
            String cursorParam = Optional.ofNullable(webRequest.getParameter(CURSOR_PARAM))
                    .orElse(String.valueOf(cursorDefault.cursor()));
            return Long.parseLong(cursorParam);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid cursor parameter", e);
        }
    }

    /**
     * Http 요청 파라미터에서 {@code size} 에 해당하는 값을 파싱합니다.
     * 없으면, 디폴트 값을 반환합니다.
     *
     * @param webRequest 현재 웹 요청 {@link NativeWebRequest}
     * @param cursorDefault 디폴트 값을 갖고 있는 {@code CursorDefault} annotation
     * @return 조회할 데이터 크기
     */
    private int getSize(NativeWebRequest webRequest, CursorDefault cursorDefault) {
        try {
            String sizeParam = Optional.ofNullable(webRequest.getParameter(SIZE_PARAM))
                    .orElse(String.valueOf(cursorDefault.size()));
            return Integer.parseInt(sizeParam);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid cursor parameter", e);
        }
    }

    /**
     * Http 요청 파라미터에서 {@code sort} 에 해당하는 값을 파싱합니다.
     * 없으면, 디폴트 값을 반환합니다.
     *
     * @param webRequest 현재 웹 요청 {@link NativeWebRequest}
     * @param cursorDefault 디폴트 값을 갖고 있는 {@code CursorDefault} annotation
     * @return {@link SortType}
     */
    private SortType getSort(NativeWebRequest webRequest, CursorDefault cursorDefault) {
        String sortParam =
                Optional.ofNullable(webRequest.getParameter(SORT_PARAM)).orElse(cursorDefault.sort());
        return sortTypeResolver.resolve(sortParam, cursorDefault.sortType());
    }

    private String getValue(NativeWebRequest webRequest, CursorDefault cursorDefault) {

        String raw = Optional.ofNullable(webRequest.getParameter(VALUE_PARAM)).orElse(cursorDefault.value());
        if (raw == null || raw.isBlank()) return null;

        return raw;
    }
}
