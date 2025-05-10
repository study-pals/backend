package com.studypals.domain.chatManage.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.dto.ChatRoomInfoRes;
import com.studypals.domain.chatManage.service.ChatRoomService;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;

/**
 * <br>package name   : com.studypals.domain.chatManage.api
 * <br>file name      : ChatRoomController
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
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/room")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @GetMapping("/{chatRoomId}")
    public ResponseEntity<Response<ChatRoomInfoRes>> getChatRoomInfo(
            @PathVariable("chatRoomId") String chatRoomId, @AuthenticationPrincipal Long userId) {
        ChatRoomInfoRes chatRoomInfo = chatRoomService.getChatRoomInfo(userId, chatRoomId);

        return ResponseEntity.ok(CommonResponse.success(ResponseCode.CHAT_ROOM_SEARCH, chatRoomInfo, chatRoomId));
    }
}
