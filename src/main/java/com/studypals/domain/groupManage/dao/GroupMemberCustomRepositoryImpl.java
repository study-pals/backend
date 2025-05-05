package com.studypals.domain.groupManage.dao;

import static com.studypals.domain.groupManage.entity.QGroupMember.groupMember;
import static com.studypals.domain.memberManage.entity.QMember.member;

import java.util.List;

import lombok.RequiredArgsConstructor;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studypals.domain.groupManage.dto.GroupMemberProfileDto;
import com.studypals.domain.groupManage.entity.GroupRole;

/**
 * group member custom repository 의 구현 클래스입니다.
 *
 * <p>group member 관련 커스텀 쿼리를 구현합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link GroupMemberCustomRepository} 의 구현 클래스입니다.
 *
 * <p><b>빈 관리:</b><br>
 * Repository
 *
 * @author s0o0bn
 * @see GroupMemberCustomRepository
 * @since 2025-04-19
 */
@RequiredArgsConstructor
public class GroupMemberCustomRepositoryImpl implements GroupMemberCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<GroupMemberProfileDto> findTopNMemberByJoinedAt(Long groupId, int limit) {
        return queryFactory
                .select(Projections.constructor(
                        GroupMemberProfileDto.class, member.id, member.nickname, member.imageUrl, groupMember.role))
                .from(groupMember)
                .join(member)
                .on(groupMember.member.id.eq(member.id))
                .where(groupMember.group.id.eq(groupId))
                .orderBy(orderByLeaderPriority(), groupMember.joinedAt.desc())
                .limit(limit)
                .fetch();
    }

    private OrderSpecifier<Integer> orderByLeaderPriority() {
        return new CaseBuilder()
                .when(groupMember.role.eq(GroupRole.LEADER))
                .then(0)
                .otherwise(1)
                .asc();
    }
}
