package com.studypals.domain.chatManage.dto;

/**
 * 유저가 읽은 메시지에 대한 정보입니다.
 * 보통 List 등을 사용하여 여러 데이터를 묶어서 처리합니다.
 *
 * @author jack8
 * @since 2025-11-18
 */
public record ChatCursorRes(Long userId, String chatId) {}
