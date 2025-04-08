package com.studypals.domain.memberManage.restDocsTest;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasKey;
import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasStatus;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.http.HttpDocumentation.httpRequest;
import static org.springframework.restdocs.http.HttpDocumentation.httpResponse;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import com.studypals.domain.memberManage.api.AuthController;
import com.studypals.domain.memberManage.dto.CreateMemberReq;
import com.studypals.domain.memberManage.dto.ReissueTokenRes;
import com.studypals.domain.memberManage.dto.SignInReq;
import com.studypals.domain.memberManage.dto.TokenReissueReq;
import com.studypals.domain.memberManage.service.MemberService;
import com.studypals.domain.memberManage.service.SignInService;
import com.studypals.domain.memberManage.service.TokenService;
import com.studypals.global.exceptions.errorCode.AuthErrorCode;
import com.studypals.global.exceptions.exception.AuthException;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;
import com.studypals.global.security.jwt.JwtToken;
import com.studypals.testModules.testSupport.RestDocsSupport;

/**
 * {@link AuthController} 에 대한 rest docs web mvc test 입니다. 문서를 생성합니다.
 *
 * @author jack8
 * @see AuthController
 * @see RestDocsSupport
 * @since 2025-04-06
 */
@WebMvcTest(AuthController.class)
class AuthControllerRestDocsTest extends RestDocsSupport {

    @MockitoBean private MemberService memberService;
    @MockitoBean private SignInService signInService;
    @MockitoBean private TokenService tokenService;

    @Test
    void register_success() throws Exception {

        // given
        CreateMemberReq req =
                new CreateMemberReq(
                        "username",
                        "password",
                        "nickname",
                        LocalDate.of(2000, 1, 1),
                        "student",
                        "example.com");

        Response<Long> expectedResponse =
                CommonResponse.success(ResponseCode.USER_CREATE, 1L, "success create user");

        given(memberService.createMember(req)).willReturn(1L);

        // when
        ResultActions result =
                mockMvc.perform(
                        post("/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)));

        // then
        result.andExpect(hasKey(expectedResponse))
                .andExpect(status().isOk())
                .andDo(
                        restDocs.document(
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
                                                .attributes(constraints("not null, unique")),
                                        fieldWithPath("birthday")
                                                .description("유저의 생일 / 2024-01-01 형식 사용")
                                                .attributes(constraints("optional")),
                                        fieldWithPath("position")
                                                .description("유저의 현재 직업, 상태 등의 그룹 이름")
                                                .attributes(constraints("optional")),
                                        fieldWithPath("imageUrl")
                                                .description("유저의 프로필 이미지")
                                                .attributes(constraints("optional"))),
                                responseFields(
                                        fieldWithPath("data").description("생성된 user의 id/식별자"),
                                        fieldWithPath("code").description("U01-01 고정"),
                                        fieldWithPath("status")
                                                .description("응답 상태 (예: success 또는 fail)"),
                                        fieldWithPath("message").description("응답 메시지"))));
    }

    @Test
    void register_fail_duplicate_user() throws Exception {
        // given
        CreateMemberReq duplicateUserReq =
                new CreateMemberReq(
                        "username",
                        "password1",
                        "nickname1",
                        LocalDate.of(2000, 1, 1),
                        "student",
                        "example.com");

        AuthErrorCode errorCode = AuthErrorCode.SIGNUP_FAIL;

        given(memberService.createMember(duplicateUserReq)).willThrow(new AuthException(errorCode));

        // when
        ResultActions result =
                mockMvc.perform(
                        post("/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(duplicateUserReq)));

        // then
        result.andExpect(hasStatus(errorCode))
                .andExpect(hasKey(errorCode))
                .andDo(restDocs.document(httpResponse()));
    }

    @Test
    void login_success() throws Exception {
        // given
        String username = "username";
        String password = "password";
        SignInReq req = new SignInReq(username, password);
        JwtToken jwtToken = new JwtToken("Bearer", "access_token", "refresh_token");

        Response<JwtToken> expectedResponse =
                CommonResponse.success(ResponseCode.USER_LOGIN, jwtToken, "success login");

        given(signInService.signInByUsernameAndPassword(username, password)).willReturn(jwtToken);
        given(memberService.getMemberIdByUsername(username)).willReturn(1L);
        willDoNothing().given(tokenService).saveRefreshToken(any());

        // when
        ResultActions result =
                mockMvc.perform(
                        post("/sign-in")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey(expectedResponse))
                .andDo(
                        restDocs.document(
                                requestBody(),
                                responseBody(),
                                requestFields(
                                        fieldWithPath("username")
                                                .description("로그인 하고자 하는 username")
                                                .attributes(constraints("not null")),
                                        fieldWithPath("password")
                                                .description("로그인 하고자 하는 password")
                                                .attributes(constraints("not null"))),
                                responseFields(
                                        fieldWithPath("code").description("U01-05 고정"),
                                        fieldWithPath("status")
                                                .description("응답 상태 (예: success 또는 fail)"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        subsectionWithPath("data").description("JWT 토큰 데이터"),
                                        fieldWithPath("data.grantType")
                                                .description("토큰 타입 (Bearer 고정)"),
                                        fieldWithPath("data.accessToken")
                                                .description("Access Token"),
                                        fieldWithPath("data.refreshToken")
                                                .description("Refresh Token"))));
    }

    @Test
    void login_fail_invalid_value() throws Exception {
        // given
        String username = "username";
        String password = "password";
        SignInReq req = new SignInReq(username, password);

        AuthErrorCode errorCode = AuthErrorCode.LOGIN_FAIL;

        given(signInService.signInByUsernameAndPassword(username, password))
                .willThrow(new AuthException(errorCode));

        // when
        ResultActions result =
                mockMvc.perform(
                        post("/sign-in")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)));

        // then
        result.andExpect(hasStatus(errorCode))
                .andExpect(hasKey(errorCode))
                .andDo(restDocs.document(responseBody()));
    }

    @Test
    void refreshToken_success() throws Exception {
        // given
        TokenReissueReq req = new TokenReissueReq("refresh_token");
        String authHeader = "Bearer access_token";

        ReissueTokenRes tokenResponse =
                ReissueTokenRes.builder()
                        .userId(1L)
                        .accessToken("new_access_token")
                        .refreshToken("new_refresh_token")
                        .build();

        Response<ReissueTokenRes> expectedResponse =
                CommonResponse.success(
                        ResponseCode.USER_REISSUE_TOKEN, tokenResponse, "success reissue token");

        given(tokenService.reissueJwtToken(any())).willReturn(tokenResponse);
        willDoNothing().given(tokenService).saveRefreshToken(any());

        // when
        ResultActions result =
                mockMvc.perform(
                        post("/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", authHeader)
                                .content(objectMapper.writeValueAsString(req)));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey(expectedResponse))
                .andDo(
                        restDocs.document(
                                requestBody(),
                                responseBody(),
                                requestHeaders(
                                        headerWithName("Authorization")
                                                .description("만료된 access token")),
                                requestFields(
                                        fieldWithPath("refreshToken")
                                                .description("유효한 refresh token")
                                                .attributes(constraints("not null"))),
                                responseFields(
                                        fieldWithPath("code").description("U01-07 고정"),
                                        fieldWithPath("status")
                                                .description("응답 상태 (예: success 또는 fail)"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("data.accessToken")
                                                .description("새 Access token"),
                                        fieldWithPath("data.refreshToken")
                                                .description("새 Refresh token"))));
    }
}
