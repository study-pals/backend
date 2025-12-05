package com.studypals.domain.chatManage.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.dto.ChatRoomInfoRes;
import com.studypals.domain.chatManage.dto.ChatRoomListRes;
import com.studypals.domain.chatManage.service.ChatRoomService;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;
import com.studypals.global.sse.SseEmitterManager;
import com.studypals.global.sse.SseSendDto;

/**
 * 채팅방 전반에 걸친 정보를 받는 엔드포인트입니다. 채팅방 정보, 로그 조회, 참여한 사용자 조회 등,
 * 특정 채팅방의 부분 정보를 호출합니다. <br>
 *
 * <pre>
 *     - GET /chat/room/{chatRoomId} : 채팅방 정보 조회
 *     - GET /chat/room/list : SSE 기반, 채팅방 리스트 조회
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
    private final SseEmitterManager sseManager;

    // 구독 이후, 해당 요청 보냄 -> 응답을 받고 정렬 마칠 때 까지, 새로운 메시지가 와도 일단 렌더링 중지, 마치고 렌더링
    @GetMapping("/{chatRoomId}")
    public ResponseEntity<Response<ChatRoomInfoRes>> getChatRoomInfo(
            @PathVariable("chatRoomId") String chatRoomId,
            @RequestParam(defaultValue = "1", name = "after") String chatId,
            @AuthenticationPrincipal Long userId) {
        ChatRoomInfoRes chatRoomInfo = chatRoomService.getChatRoomInfo(userId, chatRoomId, chatId);

        return ResponseEntity.ok(CommonResponse.success(ResponseCode.CHAT_ROOM_SEARCH, chatRoomInfo, chatRoomId));
    }

    /**
     * SSE 기반의 채팅 리스트 반환 API 입니다. 사용자가 해당 uri 를 이용해 요청을 보내는 경우,
     * {@link SseEmitterManager} 에서 관리하는 emitter 를 통해 메시지를 비동기적으로 보낼 수 있습니다. <br>
     * 해당 메서드에서는 최초 1회에 대해 init-message 타입으로 초기 채팅방 데이터를 전송합니다.  <br>
     * @param userId
     * @return
     */
    @GetMapping("/list")
    public SseEmitter getList(@AuthenticationPrincipal Long userId) {
        SseEmitter emitter = sseManager.createEmitter(userId);
        ChatRoomListRes res = chatRoomService.getChatRoomList(userId);
        sseManager.sendMessageAsync(userId, new SseSendDto("init-message", res));

        return emitter;
    }
}
