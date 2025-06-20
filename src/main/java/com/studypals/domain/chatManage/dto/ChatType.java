package com.studypals.domain.chatManage.dto;

/**
 * 채팅 메시지의 type 을 정의합니다.
 * 각 채팅 메시지 별 역할에 따라 구분됩니다.
 *
 * @author jack8
 * @see IncomingMessage
 * @see OutgoingMessage
 * @since 2025-06-19
 */
public enum ChatType {
    TEXT,
    IMAGE,
    READ,
    STAT
}
