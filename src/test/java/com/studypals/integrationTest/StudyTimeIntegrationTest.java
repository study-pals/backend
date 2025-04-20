package com.studypals.integrationTest;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;

import com.studypals.global.responses.ResponseCode;
import com.studypals.testModules.testSupport.IntegrationSupport;

/**
 * {@link com.studypals.domain.studyManage.api.StudyTimeController StudyTimeController}에 대한 통합 테스트입니다.
 * {@link IntegrationSupport} 를 상속합니다.
 *
 * @author jack8
 * @since 2025-04-14
 */
@ActiveProfiles("test")
@DisplayName("API TEST / 공부 시간 통합 테스트")
public class StudyTimeIntegrationTest extends IntegrationSupport {

    @Test
    @DisplayName("GET /studies/- 카테고리 + 시간 존재")
    void getStudyTimeByDate_success_withData() throws Exception {
        // given
        CreateUserVar user = createUser();
        LocalDate date = LocalDate.of(2025, 4, 10);

        // study_category, study_time 테이블에 직접 삽입
        jdbcTemplate.update(
                """
                INSERT INTO study_category (id, name, color, day_belong, description, member_id)
                VALUES (?, ?, ?, ?, ?, ?)
""",
                1L,
                "자바",
                "#FFCC00",
                12,
                "자바 공부",
                user.getUserId());

        jdbcTemplate.update(
                """
                INSERT INTO study_time (id, category_id, member_id, time, studied_at)
                VALUES (?, ?, ?, ?, ?)
    """,
                1L,
                1L,
                user.getUserId(),
                120L,
                date);

        // when
        ResultActions result = mockMvc.perform(get("/studies/stat")
                .param("date", "2025-04-10")
                .header("Authorization", "Bearer " + user.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey("code", ResponseCode.STUDY_TIME_PARTIAL.getCode()))
                .andExpect(jsonPath("$.data[0].categoryId").value(1L))
                .andExpect(jsonPath("$.data[0].name").value("자바"))
                .andExpect(jsonPath("$.data[0].time").value(120L));
    }

    @Test
    @DisplayName("GET /studies/stat - 기간별 공부 기록 조회 성공")
    void getStudyStatByPeriod_success() throws Exception {
        // given
        CreateUserVar user = createUser();
        Long userId = user.getUserId();
        LocalDate date1 = LocalDate.of(2025, 4, 9);
        LocalDate date2 = LocalDate.of(2025, 4, 10);

        // category 삽입
        jdbcTemplate.update(
                """
                INSERT INTO study_category (id, name, color, day_belong, description, member_id)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                100L,
                "CS",
                "#00AAFF",
                7,
                "CS 카테고리",
                userId);

        // study_time (카테고리 기반)
        jdbcTemplate.update(
                """
                INSERT INTO study_time (id, category_id, member_id, time, studied_at)
                VALUES (?, ?, ?, ?, ?)
                """,
                1L,
                100L,
                userId,
                60L,
                date1);

        // study_time (임시이름 기반)
        jdbcTemplate.update(
                """
                INSERT INTO study_time (id, temporary_name, member_id, time, studied_at)
                VALUES (?, ?, ?, ?, ?)
                """,
                2L,
                "토익",
                userId,
                90L,
                date2);

        // daily_study_info 삽입 (기간용)
        jdbcTemplate.update(
                """
                INSERT INTO daily_study_info (id, member_id, studied_at, start_at, end_at, memo)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                1L,
                userId,
                date1,
                LocalTime.of(9, 0),
                LocalTime.of(11, 0),
                "집중 잘 됨");

        jdbcTemplate.update(
                """
                INSERT INTO daily_study_info (id, member_id, studied_at, start_at, end_at, memo)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                2L,
                userId,
                date2,
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                "단어 외움");

        // when
        ResultActions result = mockMvc.perform(get("/studies/stat")
                .param("start", "2025-04-09")
                .param("end", "2025-04-10")
                .header("Authorization", "Bearer " + user.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey("code", ResponseCode.STUDY_TIME_ALL.getCode()))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].studiedDate").value("2025-04-09"))
                .andExpect(jsonPath("$.data[1].studiedDate").value("2025-04-10"))
                .andExpect(jsonPath("$.data[0].studyList.length()").value(1))
                .andExpect(jsonPath("$.data[1].studyList.length()").value(1));
    }
}
