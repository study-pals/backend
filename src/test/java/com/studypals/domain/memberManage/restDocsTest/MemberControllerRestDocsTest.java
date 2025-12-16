package com.studypals.domain.memberManage.restDocsTest;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasKey;
import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasStatus;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.http.HttpDocumentation.httpRequest;
import static org.springframework.restdocs.http.HttpDocumentation.httpResponse;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import com.studypals.domain.memberManage.api.MemberController;
import com.studypals.domain.memberManage.dto.CreateMemberReq;
import com.studypals.domain.memberManage.dto.MemberDetailsRes;
import com.studypals.domain.memberManage.dto.UpdateProfileReq;
import com.studypals.domain.memberManage.service.MemberService;
import com.studypals.global.exceptions.errorCode.AuthErrorCode;
import com.studypals.global.exceptions.exception.AuthException;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;
import com.studypals.testModules.testSupport.RestDocsSupport;

/**
 * {@link MemberController} 에 대한 rest docs web mvc test 입니다. 문서를 생성합니다.
 *
 * @author jack8
 * @see MemberController
 * @see RestDocsSupport
 * @since 2025-12-16
 */
@WebMvcTest(MemberController.class)
public class MemberControllerRestDocsTest extends RestDocsSupport {

    @MockitoBean
    private MemberService memberService;

    @Test
    void register_success() throws Exception {

        // given
        CreateMemberReq req = new CreateMemberReq("username", "password", "nickname");

        Response<Long> expectedResponse = CommonResponse.success(ResponseCode.USER_CREATE, 1L, "회원가입을 성공하였습니다.");

        given(memberService.createMember(req)).willReturn(1L);

        // when
        ResultActions result = mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // then
        result.andExpect(hasKey(expectedResponse))
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        requestFields(
                                fieldWithPath("username")
                                        .description("유저의 아이디")
                                        .attributes(constraints("not null, unique")),
                                fieldWithPath("password")
                                        .description("유저의 비밀번호")
                                        .attributes(constraints("not null")),
                                fieldWithPath("nickname")
                                        .description("유저의 닉네임")
                                        .attributes(constraints("not null, unique"))),
                        responseFields(
                                fieldWithPath("data").description("생성된 user의 id/식별자"),
                                fieldWithPath("code").description("U01-01 고정"),
                                fieldWithPath("status").description("응답 상태 (예: success 또는 fail)"),
                                fieldWithPath("message").description("응답 메시지"))));
    }

    @Test
    void register_fail_duplicate_user() throws Exception {
        // given
        CreateMemberReq duplicateUserReq = new CreateMemberReq("username", "password1", "nickname1");

        AuthErrorCode errorCode = AuthErrorCode.SIGNUP_FAIL;

        given(memberService.createMember(duplicateUserReq)).willThrow(new AuthException(errorCode));

        // when
        ResultActions result = mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateUserReq)));

        // then
        result.andExpect(hasStatus(errorCode)).andExpect(hasKey(errorCode)).andDo(restDocs.document(httpResponse()));
    }

    @Test
    @WithMockUser
    void getProfile_success() throws Exception {
        // given
        MemberDetailsRes memberDetailsRes = MemberDetailsRes.builder()
                .id(1L)
                .username("username@example.com")
                .nickname("nickname")
                .birthday(LocalDate.of(1999, 8, 20))
                .position("student")
                .imageUrl("example.image.com")
                .createdDate(LocalDate.of(2025, 1, 1))
                .token(412L)
                .build();
        given(memberService.getProfile(any())).willReturn(memberDetailsRes);

        Response<MemberDetailsRes> response = CommonResponse.success(ResponseCode.USER_UPDATE, memberDetailsRes, "");

        // when
        ResultActions result = mockMvc.perform(get("/profile").contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(hasKey(response))
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        responseFields(
                                fieldWithPath("code").description("응답 코드 (U01-02)"),
                                fieldWithPath("status").description("응답 상태"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("data").description("회원 정보"),
                                fieldWithPath("data.id").description("회원 ID"),
                                fieldWithPath("data.username").description("사용자 아이디(이메일)"),
                                fieldWithPath("data.nickname").description("닉네임"),
                                fieldWithPath("data.birthday").description("생일").optional(),
                                fieldWithPath("data.position").description("직업").optional(),
                                fieldWithPath("data.imageUrl")
                                        .description("프로필 이미지 URL")
                                        .optional(),
                                fieldWithPath("data.createdDate").description("계정 생성일"),
                                fieldWithPath("data.token").description("토큰 개수").optional())));
    }

    @Test
    @WithMockUser
    void updateProfile_success() throws Exception {
        // given
        UpdateProfileReq req = new UpdateProfileReq(LocalDate.of(1999, 8, 20), "학생", "exmaple.image.com");

        given(memberService.updateProfile(anyLong(), any(UpdateProfileReq.class)))
                .willReturn(1L);

        // when
        ResultActions result = mockMvc.perform(
                put("/profile").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)));

        // then
        result.andExpect(status().isOk())
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        requestFields(
                                fieldWithPath("birthday").description("생일").optional(),
                                fieldWithPath("position").description("직무/포지션").optional(),
                                fieldWithPath("imageUrl")
                                        .description("프로필 이미지 URL")
                                        .optional()),
                        responseFields(
                                fieldWithPath("code").description("응답 코드 (U01-02)"),
                                fieldWithPath("status").description("응답 상태"),
                                fieldWithPath("data").description("수정된 회원 ID"),
                                fieldWithPath("message").description("응답 메시지"))));
    }
}
