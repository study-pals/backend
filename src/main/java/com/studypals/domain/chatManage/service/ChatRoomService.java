package com.studypals.domain.chatManage.service;

import com.studypals.domain.chatManage.dto.ChatRoomInfoRes;

/**
 * <br>package name   : com.studypals.domain.chatManage.service
 * <br>file name      : ChatRoomService
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
public interface ChatRoomService {

    ChatRoomInfoRes getChatRoomInfo(Long userId, String chatRoomId);
}
