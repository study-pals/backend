package com.studypals.integrationTest;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;

import com.studypals.domain.studyManage.dto.EndStudyReq;
import com.studypals.domain.studyManage.dto.StartStudyReq;
import com.studypals.global.responses.ResponseCode;
import com.studypals.testModules.testSupport.IntegrationSupport;

/**
 * {@link com.studypals.domain.studyManage.api.StudySessionController StudySessionController}에 대한 통합 테스트입니다.
 * {@link IntegrationSupport} 를 상속합니다.
 *
 * @author jack8
 * @since 2025-04-14
 */
@ActiveProfiles("test")
@DisplayName("API TEST / 공부 세션 통합 테스트")
public class StudySessionIntegrationTest extends IntegrationSupport {

    @Test
    @DisplayName("POST /studies/sessions/start with categoryId")
    void startStudy_success_withCategoryId() throws Exception {
        // given
        CreateUserVar user = createUser();
        StartStudyReq req = new StartStudyReq(1L, null, LocalTime.of(9, 0));

        // when
        ResultActions result = mockMvc.perform(post("/studies/sessions/start")
                .header("Authorization", "Bearer " + user.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey("code", ResponseCode.STUDY_START.getCode()))
                .andExpect(hasKey("message", "success start"))
                .andExpect(jsonPath("$.data.studying").value(true))
                .andExpect(jsonPath("$.data.startTime").exists())
                .andExpect(jsonPath("$.data.studyTime").value(0))
                .andExpect(jsonPath("$.data.categoryId").value(1));
    }

    @Test
    @DisplayName("POST /studies/sessions/end - 시작 먼저 하고 종료")
    void endStudy_success_afterStart() throws Exception {
        // given
        CreateUserVar user = createUser();

        // 공부 시작
        StartStudyReq startReq = new StartStudyReq(null, "name", LocalTime.of(9, 0));
        ResultActions result = mockMvc.perform(post("/studies/sessions/start")
                .header("Authorization", "Bearer " + user.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(startReq)));

        result.andExpect(status().isOk());

        // 공부 종료
        EndStudyReq endReq = new EndStudyReq(LocalTime.of(10, 0));

        // when
        result = mockMvc.perform(post("/studies/sessions/end")
                .header("Authorization", "Bearer " + user.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(endReq)));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey("code", ResponseCode.STUDY_START.getCode()))
                .andExpect(hasKey("message", "success end"))
                .andExpect(jsonPath("$.data").value(3600));
    }
}
