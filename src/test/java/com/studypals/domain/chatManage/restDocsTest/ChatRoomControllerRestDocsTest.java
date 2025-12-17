package com.studypals.domain.chatManage.restDocsTest;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.http.HttpDocumentation.httpRequest;
import static org.springframework.restdocs.http.HttpDocumentation.httpResponse;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import com.studypals.domain.chatManage.api.ChatRoomController;
import com.studypals.domain.chatManage.dto.*;
import com.studypals.domain.chatManage.entity.ChatRoomRole;
import com.studypals.domain.chatManage.service.ChatRoomService;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;
import com.studypals.testModules.testSupport.RestDocsSupport;

/**
 * {@link ChatRoomController} 에 대한 rest docs test
 *
 * @author jack8
 * @since 2025-05-19
 */
@WebMvcTest(ChatRoomController.class)
class ChatRoomControllerRestDocsTest extends RestDocsSupport {

    @MockitoBean
    private ChatRoomService chatRoomService;

    @Test
    @WithMockUser
    void getChatRoomInfo_success() throws Exception {
        // given
        String chatRoomId = "study-room-1";

        ChatRoomInfoRes responseData = ChatRoomInfoRes.builder()
                .roomId(chatRoomId)
                .name("스터디 1반 단톡방")
                .userInfos(List.of(
                        ChatRoomInfoRes.UserInfo.builder()
                                .userId(1L)
                                .role(ChatRoomRole.ADMIN)
                                .imageUrl("https://cdn.example.com/profiles/user1.png")
                                .build(),
                        ChatRoomInfoRes.UserInfo.builder()
                                .userId(2L)
                                .role(ChatRoomRole.MANAGER)
                                .imageUrl("https://cdn.example.com/profiles/user2.png")
                                .build(),
                        ChatRoomInfoRes.UserInfo.builder()
                                .userId(3L)
                                .role(ChatRoomRole.MEMBER)
                                .imageUrl("https://cdn.example.com/profiles/user3.png")
                                .build()))
                .cursor(List.of(new ChatCursorRes(1L, "15"), new ChatCursorRes(2L, "14"), new ChatCursorRes(3L, "15")))
                .logs(List.of(
                        LoggingMessage.builder()
                                .id("15")
                                .type(ChatType.TEXT)
                                .content("내일 10시에 회의할까요?")
                                .sender(1L)
                                .build(),
                        LoggingMessage.builder()
                                .id("14")
                                .type(ChatType.TEXT)
                                .content("네, 가능합니다.")
                                .sender(2L)
                                .build(),
                        LoggingMessage.builder()
                                .id("13")
                                .type(ChatType.TEXT)
                                .content("저도 참석할게요.")
                                .sender(3L)
                                .build()))
                .build();

        given(chatRoomService.getChatRoomInfo(any(), any(), any())).willReturn(responseData);

        Response<ChatRoomInfoRes> expected =
                CommonResponse.success(ResponseCode.CHAT_ROOM_SEARCH, responseData, chatRoomId);

        // when
        ResultActions result = mockMvc.perform(get("/chat/room/{chatRoomId}", chatRoomId));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey(expected))
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        pathParameters(parameterWithName("chatRoomId").description("조회할 채팅방 ID")),
                        responseFields(
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("status").description("응답 상태"),
                                fieldWithPath("message").description("채팅방 ID"),
                                fieldWithPath("data.roomId").description("채팅방 ID"),
                                fieldWithPath("data.name").description("채팅방 이름"),
                                fieldWithPath("data.userInfos[].userId").description("유저 ID"),
                                fieldWithPath("data.userInfos[].role").description("유저 역할 (ADMIN | MANAGER | MEMBER)"),
                                fieldWithPath("data.userInfos[].imageUrl").description("유저 프로필 이미지 URL"),
                                fieldWithPath("data.cursor[].userId").description("해당 커서가 가리키는 유저 ID"),
                                fieldWithPath("data.cursor[].chatId").description("해당 유저가 마지막으로 읽은 채팅 ID"),
                                fieldWithPath("data.logs[].id").description("채팅 ID"),
                                fieldWithPath("data.logs[].type").description("채팅 타입 (예: TEXT)"),
                                fieldWithPath("data.logs[].content").description("채팅 메시지 내용"),
                                fieldWithPath("data.logs[].sender").description("메시지 보낸 유저 ID"))));
    }
}
