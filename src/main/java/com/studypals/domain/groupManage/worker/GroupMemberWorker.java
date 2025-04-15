package com.studypals.domain.groupManage.worker;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dao.GroupMemberRepository;
import com.studypals.domain.groupManage.dto.mappers.GroupMemberMapper;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupMember;
import com.studypals.domain.groupManage.entity.GroupRole;
import com.studypals.domain.memberManage.dao.MemberRepository;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.global.annotations.Worker;
import com.studypals.global.exceptions.errorCode.GroupErrorCode;
import com.studypals.global.exceptions.exception.GroupException;

/**
 * group member 도메인의 기본 Worker 클래스입니다.
 *
 * <p>group member 관련 CUD 로직을 수행합니다.
 *
 *
 * <p><b>빈 관리:</b><br>
 * Worker
 *
 * @author s0o0bn
 * @since 2025-04-15
 */
@Worker
@RequiredArgsConstructor
public class GroupMemberWorker {
    private final MemberRepository memberRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupMemberMapper groupMemberMapper;

    public GroupMember createLeader(Long memberId, Group group) {
        Member member = memberRepository.getReferenceById(memberId);
        return create(member, group, GroupRole.LEADER);
    }

    public GroupMember createMember(Long memberId, Group group) {
        Member member = memberRepository.getReferenceById(memberId);
        return create(member, group, GroupRole.MEMBER);
    }

    private GroupMember create(Member member, Group group, GroupRole role) {
        GroupMember groupMember = groupMemberMapper.toEntity(member, group, role);
        try {
            groupMemberRepository.save(groupMember);
        } catch (Exception e) {
            throw new GroupException(GroupErrorCode.GROUP_MEMBER_CREATE_FAIL);
        }
        return groupMember;
    }
}
