package com.studypals.domain.chatManage.restDocsTest;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.http.HttpDocumentation.httpRequest;
import static org.springframework.restdocs.http.HttpDocumentation.httpResponse;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.multipart.MultipartFile;

import com.studypals.domain.chatManage.api.ChatRoomController;
import com.studypals.domain.chatManage.dto.*;
import com.studypals.domain.chatManage.entity.ChatRoomRole;
import com.studypals.domain.chatManage.service.ChatRoomService;
import com.studypals.global.file.dto.ImageUploadRes;
import com.studypals.global.file.service.ImageFileService;
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

    @MockitoBean
    private ImageFileService imageFileService;

    private final MockMultipartFile mockMultipartFile = new MockMultipartFile(
            "file", // 컨트롤러가 받는 파라미터 변수명 (필수 확인!)
            "chat-image.png", // 업로드할 파일명
            "image/png", // 파일 타입
            "fake-image-content".getBytes() // 파일 내용 (더미)
            );

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
                                fieldWithPath("data.userInfos[].nickname").description("유저 닉네임"),
                                fieldWithPath("data.cursor[].userId").description("해당 커서가 가리키는 유저 ID"),
                                fieldWithPath("data.cursor[].chatId").description("해당 유저가 마지막으로 읽은 채팅 ID"),
                                fieldWithPath("data.logs[].id").description("채팅 ID"),
                                fieldWithPath("data.logs[].type").description("채팅 타입 (예: TEXT)"),
                                fieldWithPath("data.logs[].content").description("채팅 메시지 내용"),
                                fieldWithPath("data.logs[].sender").description("메시지 보낸 유저 ID"))));
    }

    @Test
    @WithMockUser
    @DisplayName("채팅 이미지 업로드 성공")
    void getChatUploadUrl_success() throws Exception {
        // given
        Long imageId = 1L;
        String imageUrl = "http://example.com/presigned-url-image.jpg";
        ImageUploadRes response = new ImageUploadRes(imageId, imageUrl);

        given(imageFileService.uploadChatImage(any(MultipartFile.class), any(), any()))
                .willReturn(response);

        Response<ImageUploadRes> expected = CommonResponse.success(ResponseCode.FILE_IMAGE_UPLOAD, response);

        // when
        ResultActions result =
                mockMvc.perform(multipart("/chat/room/{chatRoomId}/image", "chatRoomId-123-456") // 1. URL 템플릿 사용
                        .file(mockMultipartFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey(expected))
                .andDo(print())
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        pathParameters(parameterWithName("chatRoomId").description("채팅방 ID")),
                        requestParts(partWithName("file").description("업로드할 이미지 파일 (MultipartFile)")),
                        responseFields(
                                fieldWithPath("code").description("응답 코드 (I01-01)"),
                                fieldWithPath("status").description("응답 상태"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("data.imageId").description("이미지 파일의 식별 ID"),
                                fieldWithPath("data.imageUrl").description("저장된 이미지를 조회할 presigned url"))));
    }
}
