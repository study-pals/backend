package com.studypals.domain.groupManage.worker;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dao.GroupMemberRepository;
import com.studypals.domain.groupManage.entity.GroupMember;
import com.studypals.global.annotations.Worker;
import com.studypals.global.exceptions.errorCode.GroupErrorCode;
import com.studypals.global.exceptions.exception.GroupException;

/**
 * group 관리 권한 확인을 담당하는 Worker 클래스입니다.
 *
 * <p>사용자가 그룹에 대해 유효한 권한이 있는지 검증합니다.
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
public class GroupAuthorityValidator {
    private final GroupMemberRepository groupMemberRepository;

    public void validateLeaderAuthority(Long memberId, Long groupId) {
        GroupMember member = groupMemberRepository
                .findByMemberIdAndGroupId(memberId, groupId)
                .orElseThrow(() -> {
                    String message = String.format("member %d not found in group %d", memberId, groupId);
                    return new GroupException(GroupErrorCode.GROUP_MEMBER_NOT_FOUND, message);
                });
        if (!member.isLeader()) {
            throw new GroupException(GroupErrorCode.GROUP_FORBIDDEN);
        }
    }
}
