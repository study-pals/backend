package com.studypals.integrationTest;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;

import com.studypals.domain.memberManage.dao.RefreshTokenRedisRepository;
import com.studypals.domain.memberManage.dto.CreateMemberReq;
import com.studypals.domain.memberManage.dto.SignInReq;
import com.studypals.domain.memberManage.dto.TokenReissueReq;
import com.studypals.domain.memberManage.entity.RefreshToken;
import com.studypals.global.responses.ResponseCode;
import com.studypals.testModules.testSupport.IntegrationSupport;

/**
 * {@link com.studypals.domain.memberManage.api.AuthController AuthController} 에 대한 통합 테스트 {@link
 * IntegrationSupport} 를 사용하였다.
 *
 * @author jack8
 * @see com.studypals.domain.memberManage.api.AuthController AuthController
 * @see IntegrationSupport
 * @since 2025-04-08
 */
@ActiveProfiles("test")
@DisplayName("API TEST / 인증 통합 테스트")
public class AuthIntegrationTest extends IntegrationSupport {
    @Autowired RefreshTokenRedisRepository refreshTokenRedisRepository;

    @Test
    @DisplayName("POST /sign-in")
    void signIn_success() throws Exception {
        // given
        createUser();
        String username = "username";
        String password = "password";
        SignInReq req = new SignInReq(username, password);

        // when
        ResultActions response =
                mockMvc.perform(
                        post("/sign-in")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)));

        // then
        response.andExpect(status().isOk())
                .andExpect(hasKey("code", ResponseCode.USER_LOGIN.getCode()))
                .andExpect(hasKey("message", "success login"))
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.grantType").exists());
    }

    @Test
    @DisplayName("POST /register")
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

        // when
        ResultActions response =
                mockMvc.perform(
                        post("/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)));

        // then
        response.andExpect(status().isOk())
                .andExpect(hasKey("code", ResponseCode.USER_CREATE.getCode()))
                .andExpect(hasKey("message", "success create user"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("POST /refresh")
    void refreshToken_success() throws Exception {
        // given
        CreateUserVar var = createUser();
        TokenReissueReq req = new TokenReissueReq(var.getRefreshToken());
        String accessToken = "Bearer " + var.getAccessToken();
        RefreshToken refreshToken =
                RefreshToken.builder().id(var.getUserId()).token(var.getRefreshToken()).build();

        refreshTokenRedisRepository.save(refreshToken);

        // when
        ResultActions response =
                mockMvc.perform(
                        post("/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", accessToken)
                                .content(objectMapper.writeValueAsString(req)));

        // then
        response.andExpect(status().isOk())
                .andExpect(hasKey("code", ResponseCode.USER_REISSUE_TOKEN.getCode()))
                .andExpect(hasKey("message", "success reissue token"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }
}
