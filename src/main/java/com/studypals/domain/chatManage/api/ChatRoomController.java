package com.studypals.domain.chatManage.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.dto.ChatRoomInfoRes;
import com.studypals.domain.chatManage.service.ChatRoomService;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;

/**
 * 그룹 관리에 대한 컨트롤러입니다. 담당하는 엔드포인트는 다음과 같습니다.
 *
 * <pre>
 *     - GET /chat/room/{chatRoomId} : 채팅방 정보 조회
 * </pre>
 *
 * @author jack8
 * @since 2025-05-10
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/room")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @GetMapping("/{chatRoomId}")
    public ResponseEntity<Response<ChatRoomInfoRes>> getChatRoomInfo(
            @PathVariable("chatRoomId") String chatRoomId,
            @RequestParam(defaultValue = "1", name = "after") String chatId,
            @AuthenticationPrincipal Long userId) {
        ChatRoomInfoRes chatRoomInfo = chatRoomService.getChatRoomInfo(userId, chatRoomId, chatId);

        return ResponseEntity.ok(CommonResponse.success(ResponseCode.CHAT_ROOM_SEARCH, chatRoomInfo, chatRoomId));
    }
}
