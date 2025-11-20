package com.studypals.domain.chatManage.dto;

import java.util.List;

import lombok.Builder;

import com.studypals.domain.chatManage.entity.ChatRoomRole;

/**
 * 채팅방 정보에 대한 dto 입니다.
 * <p>
 * 내부적으로 UserInfo 를 정의하여 유저의 id, role, imageUrl 등을 리스트로 관리합니다.
 *
 * @author jack8
 * @since 2025-05-10
 */
@Builder
public record ChatRoomInfoRes(String id, String name, List<UserInfo> userInfos, List<ChatCursorRes> cursor) {

    @Builder
    public record UserInfo(Long userId, ChatRoomRole role, String imageUrl) {}
}
