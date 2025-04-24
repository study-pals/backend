package com.studypals.domain.groupManage.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dto.GroupEntryInfo;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.worker.GroupEntryCodeManager;
import com.studypals.domain.groupManage.worker.GroupMemberWorker;
import com.studypals.domain.groupManage.worker.GroupReader;
import com.studypals.global.exceptions.errorCode.GroupErrorCode;
import com.studypals.global.exceptions.exception.GroupException;

@Service
@RequiredArgsConstructor
public class GroupEntryServiceImpl implements GroupEntryService {
    private GroupReader groupReader;
    private GroupMemberWorker groupMemberWorker;
    private GroupEntryCodeManager entryCodeManager;

    @Override
    @Transactional
    public Long joinGroup(Long userId, GroupEntryInfo entryInfo) {
        Group group = groupReader.getById(entryInfo.groupId());
        if (!group.isOpen()) {
            throw new GroupException(GroupErrorCode.GROUP_JOIN_FAIL, "can't join without permission");
        }

        group.joinNewMember();
        entryCodeManager.validateCode(group.getId(), entryInfo.entryCode());
        return groupMemberWorker.createMember(userId, group).getId();
    }
}
