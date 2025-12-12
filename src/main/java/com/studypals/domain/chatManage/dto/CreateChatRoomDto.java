package com.studypals.domain.chatManage.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 채팅방 생성 시 사용하는 요청에 대한 dto 입니다.
 * <p>
 * {@code name} : NotBlank
 *
 * @author jack8
 * @since 2025-05-10
 */
public record CreateChatRoomDto(@NotBlank String name, String imageUrl) {}
