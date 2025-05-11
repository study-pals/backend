package com.studypals.integrationTest;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasKey;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Objects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.studypals.domain.groupManage.entity.GroupRole;
import com.studypals.domain.studyManage.entity.StudyType;
import com.studypals.global.responses.ResponseCode;

/**
 * {@link com.studypals.domain.groupManage.api.GroupStudyFacadeController} 에 대한 통합 테스트 {@link
 * AbstractGroupIntegrationTest} 를 사용하였다.
 *
 * @author s0o0bn
 * @see com.studypals.domain.groupManage.api.GroupStudyFacadeController
 * @see AbstractGroupIntegrationTest
 * @since 2025-05-11
 */
@ActiveProfiles("test")
@DisplayName("API TEST / 그룹 공부 관리 통합 테스트")
public class GroupStudyIntegrationTest extends AbstractGroupIntegrationTest {

    @Test
    @DisplayName("GET /group/{groupId}/routines/daily-goal")
    void getGroupDailyGoal_success() throws Exception {
        // given
        final int STUDY_TIME_GOAL_MINUTE = 120; // 목표 시간 (분단위) : 2시간

        CreateUserVar leader = createUser("leader", "leader");
        CreateUserVar member = createUser("member", "member");
        CreateGroupVar group = createGroup(leader.getUserId(), "group", "tag");
        createGroupMember(member, group);
        long categoryId = createWeeklyRoutine(group.groupId(), "category", STUDY_TIME_GOAL_MINUTE);
        createStudyTime(leader.getUserId(), categoryId, LocalDate.now(), STUDY_TIME_GOAL_MINUTE * 60); // 2시간 공부
        createStudyTime(member.getUserId(), categoryId, LocalDate.now(), (STUDY_TIME_GOAL_MINUTE - 60) * 60); // 1시간 공부

        // when
        ResultActions result =
                mockMvc.perform(MockMvcRequestBuilders.get("/groups/" + group.groupId() + "/routines/daily-goal")
                        .header("Authorization", "Bearer " + member.getAccessToken()));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey("code", ResponseCode.GROUP_DAILY_GOAL.getCode()))
                .andExpect(jsonPath("$.data.totalMember").value(2))
                .andExpect(jsonPath("$.data.categories[0].categoryId").value(categoryId))
                .andExpect(jsonPath("$.data.categories[0].goalTime").value(STUDY_TIME_GOAL_MINUTE))
                .andExpect(jsonPath("$.data.categories[0].successRate").value(0.5))
                .andExpect(jsonPath("$.data.categories[0].profiles.length()").value(1));
    }

    private void createGroupMember(CreateUserVar member, CreateGroupVar group) {
        String sql =
                """
                INSERT INTO group_member(member_id, group_id, role, joined_at)
                VALUES(?, ?, ?, ?)
                """;
        jdbcTemplate.update(sql, member.getUserId(), group.groupId(), GroupRole.MEMBER.name(), LocalDate.now());

        sql = """
                UPDATE `group` SET total_member = 2
                """;
        jdbcTemplate.update(sql);
    }

    private long createWeeklyRoutine(long groupId, String name, int goalTime) {
        String sql =
                """
                INSERT INTO group_study_category(group_id, name, goal_time, day_belong)
                VALUES(?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        // jdbcTemplate.update(sql, groupId, name, goalTime, 0);

        jdbcTemplate.update(
                con -> {
                    PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    ps.setLong(1, groupId);
                    ps.setString(2, name);
                    ps.setInt(3, goalTime);
                    ps.setInt(4, 0);
                    return ps;
                },
                keyHolder);

        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    private void createStudyTime(long userId, long categoryId, LocalDate date, long time) {
        String sql =
                """
                INSERT INTO study_time(member_id, study_type, type_id, studied_date, time)
                VALUES(?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(sql, userId, StudyType.GROUP.name(), categoryId, date, time);
    }
}
