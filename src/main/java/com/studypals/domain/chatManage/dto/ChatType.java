package com.studypals.domain.chatManage.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 채팅 메시지의 type 을 정의합니다.
 * 각 채팅 메시지 별 역할에 따라 구분됩니다.
 *
 * @author jack8
 * @see IncomingMessage
 * @see OutgoingMessage
 * @since 2025-06-19
 */
@RequiredArgsConstructor
@Getter
public enum ChatType {
    TEXT(1),
    IMAGE(2),
    READ(3),
    STAT(4),
    LOG_REQ(5),
    LOG_RES(6);

    private final int subtype;
}
