package com.studypals.domain.groupManage.worker;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dao.GroupMemberRepository;
import com.studypals.domain.groupManage.dao.GroupRepository;
import com.studypals.domain.groupManage.dto.mappers.GroupMemberMapper;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupMember;
import com.studypals.domain.groupManage.entity.GroupRole;
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
public class GroupMemberWriter {
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupMemberMapper groupMemberMapper;

    public GroupMember createLeader(Member member, Group group) {
        return create(member, group, GroupRole.LEADER);
    }

    public GroupMember createMember(Member member, Group group) {
        int updated = groupRepository.increaseGroupMember(group.getId());
        if (updated == 0) {
            throw new GroupException(GroupErrorCode.GROUP_JOIN_FAIL, "group member limit exceeded");
        }

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

    /**
     * 그룹의 리더를 다른 멤버로 위임합니다.
     *
     * <p>현재 리더(userId)가 그룹 내 다른 멤버(nextLeaderId)를 새로운 리더로 승격하고,
     * 본인은 일반 멤버로 강등됩니다.</p>
     *
     * <p>동시성 제어를 위해 현재 리더 조회 시
     * {@code findByMemberIdAndGroupIdForUpdate} 를 사용하여
     * 해당 GroupMember row에 대해 비관적 락(PESSIMISTIC_WRITE)을 획득합니다.</p>
     *
     * <p>이로 인해 동일 그룹에 대해 동시에 여러 리더 승격 요청이 들어오는 경우,
     * 이전 트랜잭션이 종료될 때까지 대기하게 되며,
     * 이미 리더에서 내려온 멤버는 리더 검증 단계에서 실패하게 됩니다.</p>
     *
     * @param groupId       리더를 위임할 그룹 ID
     * @param userId        현재 리더의 멤버 ID
     * @param nextLeaderId  새로 리더가 될 멤버 ID
     *
     * @throws GroupException
     * <ul>
     *   <li>{@link GroupErrorCode#GROUP_PROMOTE_FAIL}
     *       - 자기 자신에게 리더 위임 시도
     *       - 리더가 아닌 멤버가 리더 위임 시도</li>
     *   <li>{@link GroupErrorCode#GROUP_MEMBER_NOT_FOUND}
     *       - 현재 리더 또는 다음 리더가 그룹에 존재하지 않는 경우</li>
     * </ul>
     */
    public void promoteLeader(Long groupId, Long userId, Long nextLeaderId) {
        if (userId.equals(nextLeaderId)) {
            throw new GroupException(
                    GroupErrorCode.GROUP_PROMOTE_FAIL, "[GroupMemberWriter#promoteLeader] can't promote to myself");
        }
        GroupMember leader = groupMemberRepository
                .findByMemberIdAndGroupIdForUpdate(userId, groupId)
                .orElseThrow(() -> {
                    String message = String.format("member %d not found in group %d", userId, groupId);
                    return new GroupException(GroupErrorCode.GROUP_MEMBER_NOT_FOUND, message);
                });
        if (!leader.isLeader()) {
            throw new GroupException(GroupErrorCode.GROUP_PROMOTE_FAIL, "[GroupMemberWriter#promoteLeader] not leader");
        }

        GroupMember nextLeader = groupMemberRepository
                .findByMemberIdAndGroupId(nextLeaderId, groupId)
                .orElseThrow(() -> {
                    String message = String.format(
                            "[GroupMemberWriter#promoteReader] member %d not found in group %d", nextLeaderId, groupId);
                    return new GroupException(GroupErrorCode.GROUP_MEMBER_NOT_FOUND, message);
                });

        nextLeader.promoteToLeader();
        leader.demoteToMember();
    }
}
