package com.studypals.global.file.restDocsTest;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.http.HttpDocumentation.httpRequest;
import static org.springframework.restdocs.http.HttpDocumentation.httpResponse;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import com.studypals.global.file.api.ImageFileController;
import com.studypals.global.file.dto.ChatPresignedUrlReq;
import com.studypals.global.file.dto.PresignedUrlRes;
import com.studypals.global.file.dto.ProfilePresignedUrlReq;
import com.studypals.global.file.service.ImageFileService;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;
import com.studypals.testModules.testSupport.RestDocsSupport;

@WebMvcTest(ImageFileController.class)
class ImageFileControllerRestDocsTest extends RestDocsSupport {

    @MockitoBean
    private ImageFileService imageFileService;

    @Test
    @WithMockUser
    @DisplayName("프로필 이미지 업로드용 Presigned URL 요청")
    void getProfileUploadUrl_success() throws Exception {
        // given
        ProfilePresignedUrlReq request = new ProfilePresignedUrlReq("my-profile.jpeg");
        Long imageId = 1L;
        String presignedUrl = "https://s3-presigned-url.com/for/profile/my-profile.jpeg?signature=...";

        PresignedUrlRes response = new PresignedUrlRes(imageId, presignedUrl);
        given(imageFileService.getProfileUploadUrl(any(ProfilePresignedUrlReq.class), any()))
                .willReturn(response);

        Response<PresignedUrlRes> expected = CommonResponse.success(ResponseCode.FILE_IMAGE_UPLOAD, response);

        // when
        ResultActions result = mockMvc.perform(post("/files/image/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey(expected))
                .andDo(print())
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        requestFields(fieldWithPath("fileName")
                                .description("업로드할 파일 이름 (확장자 포함)")
                                .attributes(constraints("not null, not blank"))),
                        responseFields(
                                fieldWithPath("code").description("응답 코드 (I01-01)"),
                                fieldWithPath("status").description("응답 상태"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("data.id").description("이미지 파일의 식별 ID"),
                                fieldWithPath("data.url").description("생성된 Presigned URL"))));
    }

    @Test
    @WithMockUser
    @DisplayName("채팅 이미지 업로드용 Presigned URL 요청")
    void getChatUploadUrl_success() throws Exception {
        // given
        ChatPresignedUrlReq request = new ChatPresignedUrlReq("chat-image.png", "chat-room-123");
        String presignedUrl = "https://s3-presigned-url.com/for/chat/chat-image.png?signature=...";
        Long imageId = 1L;
        PresignedUrlRes response = new PresignedUrlRes(imageId, presignedUrl);

        given(imageFileService.getChatUploadUrl(any(ChatPresignedUrlReq.class), any()))
                .willReturn(response);

        Response<PresignedUrlRes> expected = CommonResponse.success(ResponseCode.FILE_IMAGE_UPLOAD, response);

        // when
        ResultActions result = mockMvc.perform(post("/files/image/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey(expected))
                .andDo(print())
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        requestFields(
                                fieldWithPath("fileName")
                                        .description("업로드할 파일 이름 (확장자 포함)")
                                        .attributes(constraints("not null, not blank")),
                                fieldWithPath("chatRoomId")
                                        .description("업로드 대상 채팅방 ID")
                                        .attributes(constraints("not null, not blank"))),
                        responseFields(
                                fieldWithPath("code").description("응답 코드 (I01-01)"),
                                fieldWithPath("status").description("응답 상태"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("data.id").description("이미지 파일의 식별 ID"),
                                fieldWithPath("data.url").description("생성된 Presigned URL"))));
    }
}
