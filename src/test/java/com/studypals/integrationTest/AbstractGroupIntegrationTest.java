package com.studypals.integrationTest;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import lombok.Builder;

import com.studypals.domain.groupManage.dao.GroupEntryCodeRedisRepository;
import com.studypals.testModules.testSupport.IntegrationSupport;

/**
 * 그룹 관련 도메인에 대한 통합 테스트의 추상 클래스. {@link
 * IntegrationSupport} 를 사용하였다.
 *
 * @author s0o0bn
 * @see IntegrationSupport
 * @since 2025-04-25
 */
public abstract class AbstractGroupIntegrationTest extends IntegrationSupport {
    @Autowired
    protected GroupEntryCodeRedisRepository entryCodeRedisRepository;

    protected CreateGroupVar createGroup(Long userId, String name, String tag) {
        return createGroup(userId, name, tag, true);
    }

    protected CreateGroupVar createGroup(Long userId, String name, String tag, boolean isApprovalRequired) {
        String insertQuery =
                """
                INSERT INTO `group` (name, tag, is_approval_required)
                VALUE(?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                con -> {
                    PreparedStatement ps = con.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, name);
                    ps.setString(2, tag);
                    ps.setBoolean(3, isApprovalRequired);
                    return ps;
                },
                keyHolder);

        Long groupId = Objects.requireNonNull(keyHolder.getKey()).longValue();

        String memberInsertQuery =
                """
                INSERT INTO group_member (member_id, group_id, role, joined_at)
                VALUE(?, ?, ?, ?)
                """;
        jdbcTemplate.update(memberInsertQuery, userId, groupId, "LEADER", LocalDate.now());

        return CreateGroupVar.builder().groupId(groupId).name(name).tag(tag).build();
    }

    @Builder
    protected record CreateGroupVar(Long groupId, String name, String tag) {}
}
