package com.studypals.domain.chatManage.entity;

import lombok.RequiredArgsConstructor;

/**
 * SSE type(name) 에 들어가는 내용을 enum 으로 구조화하였습니다.
 *
 * @author jack8
 * @see com.studypals.global.sse.SseEmitterManager SseEmitterManager
 * @since 2025-12-11
 */
@RequiredArgsConstructor
public enum ChatSseType {
    CONNECT("connect"),
    INIT_MESSAGE("init-message"),
    NEW_MESSAGE("new-message");

    private final String name;
}
