package com.studypals.global.file.restDocsTest;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.http.HttpDocumentation.httpRequest;
import static org.springframework.restdocs.http.HttpDocumentation.httpResponse;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.multipart.MultipartFile;

import com.studypals.global.file.api.ImageFileController;
import com.studypals.global.file.dto.ImageUploadRes;
import com.studypals.global.file.service.ImageFileService;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;
import com.studypals.testModules.testSupport.RestDocsSupport;

@WebMvcTest(ImageFileController.class)
class ImageFileControllerRestDocsTest extends RestDocsSupport {

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
    @DisplayName("프로필 이미지 업로드 성공")
    void getProfileUploadUrl_success() throws Exception {
        // given
        Long imageId = 1L;
        String imageUrl = "http://example.com/image.jpg";

        ImageUploadRes response = new ImageUploadRes(imageId, imageUrl);
        given(imageFileService.uploadProfileImage(any(MultipartFile.class), any()))
                .willReturn(response);

        Response<ImageUploadRes> expected = CommonResponse.success(ResponseCode.FILE_IMAGE_UPLOAD, response);

        // when
        ResultActions result = mockMvc.perform(
                multipart("/files/image/profile").file(mockMultipartFile).contentType(MediaType.MULTIPART_FORM_DATA));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey(expected))
                .andDo(print())
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        requestParts(partWithName("file").description("업로드할 이미지 파일 (MultipartFile)")),
                        responseFields(
                                fieldWithPath("code").description("응답 코드 (I01-01)"),
                                fieldWithPath("status").description("응답 상태"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("data.imageId").description("이미지 파일의 식별 ID"),
                                fieldWithPath("data.imageUrl").description("저장된 이미지 주소"))));
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
        ResultActions result = mockMvc.perform(multipart("/files/image/chat")
                .file(mockMultipartFile)
                .queryParam("chatRoomId", "chatRoomId-123-456")
                .contentType(MediaType.MULTIPART_FORM_DATA));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey(expected))
                .andDo(print())
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        queryParameters(parameterWithName("chatRoomId").description("채팅방 식별자 ID")),
                        requestParts(partWithName("file").description("업로드할 이미지 파일 (MultipartFile)")),
                        responseFields(
                                fieldWithPath("code").description("응답 코드 (I01-01)"),
                                fieldWithPath("status").description("응답 상태"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("data.imageId").description("이미지 파일의 식별 ID"),
                                fieldWithPath("data.imageUrl").description("저장된 이미지를 조회할 presigned url"))));
    }
}
