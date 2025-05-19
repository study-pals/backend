package com.studypals.domain.chatManage.restDocsTest;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.http.HttpDocumentation.httpRequest;
import static org.springframework.restdocs.http.HttpDocumentation.httpResponse;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import com.studypals.domain.chatManage.api.ChatRoomController;
import com.studypals.domain.chatManage.dto.ChatRoomInfoRes;
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
public class ChatRoomControllerRestDocsTest extends RestDocsSupport {

    @MockitoBean
    private ChatRoomService chatRoomService;

    @Test
    @WithMockUser
    void getChatRoomInfo_success() throws Exception {
        // given
        String chatRoomId = "chatroom";
        ChatRoomInfoRes responseData = ChatRoomInfoRes.builder()
                .id(chatRoomId)
                .name("chatRoom")
                .userInfos(List.of(
                        ChatRoomInfoRes.UserInfo.builder()
                                .userId(1L)
                                .role(ChatRoomRole.ADMIN)
                                .imageUrl("image1.img")
                                .build(),
                        ChatRoomInfoRes.UserInfo.builder()
                                .userId(2L)
                                .role(ChatRoomRole.MANAGER)
                                .imageUrl("image2.png")
                                .build(),
                        ChatRoomInfoRes.UserInfo.builder()
                                .userId(3L)
                                .role(ChatRoomRole.MEMBER)
                                .imageUrl("image3.jpg")
                                .build()))
                .build();
        given(chatRoomService.getChatRoomInfo(any(), any())).willReturn(responseData);
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
                                fieldWithPath("message").description("채팅방 id"),
                                fieldWithPath("data.id").description("채팅방 id"),
                                fieldWithPath("data.name").description("채팅방 이름"),
                                fieldWithPath("data.userInfos[].userId").description("유저 ID"),
                                fieldWithPath("data.userInfos[].role").description("유저 역할 (ADMIN | MANAGER | MEMBER)"),
                                fieldWithPath("data.userInfos[].imageUrl").description("유저 이미지 URL"))));
    }
}
