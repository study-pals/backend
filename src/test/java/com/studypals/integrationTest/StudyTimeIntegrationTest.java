package com.studypals.integrationTest;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigInteger;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
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
    @DisplayName("GET /studies/stat- 카테고리 + 시간 존재")
    void getStudyTimeByDate_success_withData() throws Exception {
        // given
        CreateUserVar user = createUser();
        LocalDate date = LocalDate.of(2025, 4, 10);
        Long categoryKey = createCategory(user.getUserId(), "category", 1200L);
        createStudyTime(user.getUserId(), categoryKey, date, 1000L);

        // when
        ResultActions result = mockMvc.perform(get("/studies/stat")
                .param("date", "2025-04-10")
                .header("Authorization", "Bearer " + user.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey("code", ResponseCode.STUDY_TIME_PARTIAL.getCode()))
                .andExpect(jsonPath("$.data[0].categoryId").value(categoryKey))
                .andExpect(jsonPath("$.data[0].time").value(1000L));
    }

    @Test
    @DisplayName("GET /studies/stat - 기간별 공부 기록 조회 성공")
    void getStudyStatByPeriod_success() throws Exception {
        // given
        CreateUserVar user = createUser();
        Long userId = user.getUserId();
        LocalDate date1 = LocalDate.of(2025, 4, 9);
        LocalDate date2 = LocalDate.of(2025, 4, 10);

        Long ck1 = createCategory(userId, "category1", 1200L);
        Long ck2 = createCategory(userId, "category2", 3600L);

        createStudyTime(userId, ck1, date1, 1000L);
        createStudyTime(userId, ck2, date1, 2000L);

        createStudyTime(userId, ck1, date2, 1000L);

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
                .andExpect(jsonPath("$.data[0].studyList.length()").value(2))
                .andExpect(jsonPath("$.data[1].studyList.length()").value(1));
    }

    private Long createCategory(Long userId, String name, Long goal) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(
                con -> {
                    PreparedStatement ps = con.prepareStatement(
                            """
                INSERT INTO study_category (study_type, type_id, date_type, name, color, day_belong, description)
                VALUES ('PERSONAL', ?, 'DAILY', ?,'#000000', ?, '테스트 설명')
            """,
                            Statement.RETURN_GENERATED_KEYS);
                    ps.setLong(1, userId);
                    ps.setString(2, name);
                    ps.setLong(3, goal);
                    return ps;
                },
                kh);
        return kh.getKeyAs(BigInteger.class).longValue();
    }

    private Long createStudyTime(Long userId, Long categoryId, LocalDate date, Long time) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(
                con -> {
                    PreparedStatement ps = con.prepareStatement(
                            """
            INSERT INTO study_time(studied_date, goal, member_id, study_category_id, time, name)
            VALUES (?, 1200, ?, ?, ?, null)
        """,
                            Statement.RETURN_GENERATED_KEYS);
                    ps.setDate(1, Date.valueOf(date));
                    ps.setLong(2, userId);
                    ps.setLong(3, categoryId);
                    ps.setLong(4, time);

                    return ps;
                },
                kh);
        return kh.getKeyAs(BigInteger.class).longValue();
    }
}
