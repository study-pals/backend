package com.studypals.domain.groupManage.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dto.GroupEntryReq;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.worker.GroupEntryCodeManager;
import com.studypals.domain.groupManage.worker.GroupEntryRequestWorker;
import com.studypals.domain.groupManage.worker.GroupMemberWorker;
import com.studypals.domain.groupManage.worker.GroupReader;
import com.studypals.global.exceptions.errorCode.GroupErrorCode;
import com.studypals.global.exceptions.exception.GroupException;

/**
 * group entry service 의 구현 클래스입니다.
 *
 * <p>group 도메인에 대한 가입 관련 로직을 수행합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link GroupEntryService} 의 구현 클래스입니다.
 *
 * <p><b>빈 관리:</b><br>
 * Service
 *
 * @author s0o0bn
 * @see GroupEntryService
 * @since 2025-04-25
 */
@Service
@RequiredArgsConstructor
public class GroupEntryServiceImpl implements GroupEntryService {
    private final GroupReader groupReader;
    private final GroupMemberWorker groupMemberWorker;
    private final GroupEntryCodeManager entryCodeManager;
    private final GroupEntryRequestWorker entryRequestWorker;

    @Override
    @Transactional
    public Long joinGroup(Long userId, GroupEntryReq entryInfo) {
        Group group = groupReader.getById(entryInfo.groupId());
        if (group.isApprovalRequired()) {
            throw new GroupException(GroupErrorCode.GROUP_JOIN_FAIL, "can't join without permission");
        }

        group.joinNewMember();
        entryCodeManager.validateCode(group.getId(), entryInfo.entryCode());
        return groupMemberWorker.createMember(userId, group).getId();
    }

    @Override
    @Transactional
    public Long requestParticipant(Long userId, GroupEntryReq entryInfo) {
        Group group = groupReader.getById(entryInfo.groupId());
        if (!group.isApprovalRequired()) {
            throw new GroupException(GroupErrorCode.GROUP_JOIN_FAIL, "should join without permission");
        }

        entryCodeManager.validateCode(group.getId(), entryInfo.entryCode());
        return entryRequestWorker.createRequest(userId, group).getId();
    }
}
