package com.studypals.domain.chatManage.dto;

import java.util.List;

import com.studypals.domain.chatManage.entity.ChatRoomRole;

/**
 * <br>package name   : com.studypals.domain.chatManage.dto
 * <br>file name      : ChatRoomInfoDto
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
public record ChatRoomInfoDto(String id, String name, List<UserInfo> userInfos) {
    public record UserInfo(Long userId, ChatRoomRole role) {}
}
