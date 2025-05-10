package com.studypals.domain.chatManage.dto;

import java.util.List;

import lombok.Builder;

import com.studypals.domain.chatManage.entity.ChatRoomRole;

/**
 * <br>package name   : com.studypals.domain.chatManage.dto
 * <br>file name      : ChatRoomInfoRes
 * <br>date           : 5/10/25
 * <pre>
 * <span style="color: white;">[description]</span>
 *
 * </pre>
 * <pre>
 * <span style="color: white;">usage:</span>
 * {@code
 *
 * } </pre>
 */
@Builder
public record ChatRoomInfoRes(String id, String name, List<UserInfo> userInfos) {

    @Builder
    public record UserInfo(Long userId, ChatRoomRole role, String imageUrl) {}
}
