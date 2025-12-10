package com.studypals.domain.chatManage.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import com.studypals.domain.chatManage.dto.ChatRoomListRes;
import com.studypals.domain.chatManage.service.ChatRoomService;
import com.studypals.global.sse.SseEmitterManager;
import com.studypals.testModules.testSupport.RestDocsSupport;

/**
 * {@link SseChatRoomController} 에 대한 rest docs test 이나, mockMvc 등이 SSE 를 테스트하는데 있어서
 * 부적절하므로, 기본적인 연결 테스트만 수행하였습니다.
 *
 * @author jack8
 * @since 2025-12-10
 */
@WebMvcTest(SseChatRoomController.class)
class SseChatRoomControllerRestDocsTest extends RestDocsSupport {

    @MockitoBean
    private ChatRoomService chatRoomService;

    @MockitoBean
    private SseEmitterManager sseEmitterManager;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser
    void getList_success() throws Exception {
        Long userId = 1L;
        SseEmitter emitter = new SseEmitter(0L);

        ChatRoomListRes chatRoomListRes = new ChatRoomListRes(List.of(
                new ChatRoomListRes.ChatRoomInfo(
                        "0bbdf1fb-a727-4402-9fd8-c4370b7350c1",
                        "chat-room-1-name",
                        "image1.example.com",
                        100,
                        12L,
                        "last message",
                        "1418c88e50171000",
                        3L),
                new ChatRoomListRes.ChatRoomInfo(
                        "0bbdf1fb-a727-4402-9fd8-c4370b7350c1",
                        "chat-room-2-name",
                        "image2.example.com",
                        15,
                        -1L,
                        null,
                        null,
                        3L)));

        given(sseEmitterManager.createEmitter(any())).willReturn(emitter);
        given(chatRoomService.getChatRoomList(any())).willReturn(chatRoomListRes);

        mockMvc.perform(get("/sse/chat/room/list").accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted())
                .andReturn();
    }
}
