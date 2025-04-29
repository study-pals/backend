package com.studypals.domain.groupManage.worker;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dao.GroupEntryRequestRepository;
import com.studypals.domain.groupManage.dto.mappers.GroupEntryRequestMapper;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupEntryRequest;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.global.annotations.Worker;
import com.studypals.global.exceptions.errorCode.GroupErrorCode;
import com.studypals.global.exceptions.exception.GroupException;

/**
 * group entry request 도메인의 기본 Worker 클래스입니다.
 *
 * <p>group entry request 관련 CUD 로직을 수행합니다.
 *
 *
 * <p><b>빈 관리:</b><br>
 * Worker
 *
 * @author s0o0bn
 * @since 2025-04-25
 */
@Worker
@RequiredArgsConstructor
public class GroupEntryRequestWorker {
    private final GroupEntryRequestRepository entryRequestRepository;
    private final GroupEntryRequestMapper mapper;

    public void validateNewRequestAvailable(Group group) {
        if (group.isFullMember()) {
            throw new GroupException(GroupErrorCode.GROUP_JOIN_FAIL, "group already full of member");
        }
        if (!group.isApprovalRequired()) {
            throw new GroupException(GroupErrorCode.GROUP_JOIN_FAIL, "should join without permission");
        }
    }

    public GroupEntryRequest createRequest(Member member, Group group) {
        GroupEntryRequest entryRequest = mapper.toEntity(member, group);
        entryRequestRepository.save(entryRequest);

        return entryRequest;
    }
}
