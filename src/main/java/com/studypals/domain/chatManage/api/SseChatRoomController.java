package com.studypals.domain.chatManage.api;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.dto.ChatRoomListRes;
import com.studypals.domain.chatManage.service.ChatRoomService;
import com.studypals.global.sse.SseEmitterManager;
import com.studypals.global.sse.SseSendDto;

/**
 *
 * <pre>
 *     - GET /chat/room/list : SSE 기반, 채팅방 리스트 조회
 * </pre>
 *
 * @author jack8
 * @since 2025-12-10
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/sse/chat/room")
public class SseChatRoomController {

    private final ChatRoomService chatRoomService;
    private final SseEmitterManager sseManager;

    @GetMapping(value = "/list", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getList(@AuthenticationPrincipal Long userId) {
        SseEmitter emitter = sseManager.createEmitter(userId);
        ChatRoomListRes res = chatRoomService.getChatRoomList(userId);
        sseManager.sendMessageAsync(userId, new SseSendDto("init-message", res));

        return emitter;
    }
}
