package com.studypals.global.security.config;

import java.util.List;

import lombok.Getter;

/**
 * public 한 접근 권한을 가지는 엔드포인트를 정의합니다.
 *
 * <p>enum으로 직접적인 링크를 관리합니다.
 *
 * @author jack8
 * @since 2025-04-02
 */
@Getter
public enum AccessURL {
    PUBLIC(List.of("/sign-in", "/register", "/first-page", "/refresh"));

    private final List<String> urls;

    AccessURL(List<String> urls) {
        this.urls = urls;
    }
}
