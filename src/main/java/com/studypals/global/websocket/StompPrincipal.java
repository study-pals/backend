package com.studypals.global.websocket;

import java.security.Principal;

/**
 * websocket 연결에 대한 정보를 저장하기 위한 객체입니다. Principal 의 구현체로서,
 * websocket interceptor 에서 세션에 대해 user로서 설정할 수 있습니다.
 *
 * 반환 타입이 String 이라 name 을 String 으로 잡기는 하였으나 userId가 들어갑니다.
 *
 * @author jack8
 * @since 2025-06-19
 */
public class StompPrincipal implements Principal {
    private final String userId;

    public StompPrincipal(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        this.userId = userId.toString();
    }

    @Override
    public String getName() {
        return userId;
    }
}
