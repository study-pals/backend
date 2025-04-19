package com.studypals.domain.groupManage.dao;

import static com.studypals.domain.groupManage.entity.QGroupMember.groupMember;
import static com.studypals.domain.memberManage.entity.QMember.member;

import java.util.List;

import lombok.RequiredArgsConstructor;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studypals.domain.groupManage.dto.GroupMemberProfileImageDto;
import com.studypals.domain.groupManage.entity.GroupRole;

@RequiredArgsConstructor
public class GroupMemberCustomRepositoryImpl implements GroupMemberCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<GroupMemberProfileImageDto> findTopNMemberByJoinedAt(Long groupId, int limit) {
        // 그룹장 조회
        GroupMemberProfileImageDto leader = queryFactory
                .select(Projections.constructor(GroupMemberProfileImageDto.class, member.imageUrl, groupMember.role))
                .from(groupMember)
                .join(member)
                .on(groupMember.member.id.eq(member.id))
                .where(groupMember.group.id.eq(groupId), groupMember.role.eq(GroupRole.LEADER))
                .fetchOne();

        // 일반 멤버 조회
        List<GroupMemberProfileImageDto> members = queryFactory
                .select(Projections.constructor(GroupMemberProfileImageDto.class, member.imageUrl, groupMember.role))
                .from(groupMember)
                .join(member)
                .on(groupMember.member.id.eq(member.id))
                .where(groupMember.group.id.eq(groupId), groupMember.role.eq(GroupRole.MEMBER))
                .orderBy(groupMember.joinedAt.desc())
                .limit(limit - 1)
                .fetch();
        members.add(leader);

        return members;
    }
}
