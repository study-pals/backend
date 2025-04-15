package com.studypals.domain.groupManage.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dto.CreateGroupReq;
import com.studypals.domain.groupManage.dto.GetGroupTagRes;
import com.studypals.domain.groupManage.dto.GroupEntryCodeRes;
import com.studypals.domain.groupManage.dto.mappers.GroupMapper;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.worker.*;

/**
 * group service 의 구현 클래스입니다.
 *
 * <p>group 도메인에 대한 전반적인 로직을 수행합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link GroupService} 의 구현 클래스입니다.
 *
 * <p><b>빈 관리:</b><br>
 * Service
 *
 * @author s0o0bn
 * @see GroupService
 * @since 2025-04-12
 */
@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {
    private final GroupWorker groupWorker;
    private final GroupReader groupReader;
    private final GroupMemberWorker groupMemberWorker;
    private final GroupAuthorityValidator authorityValidator;
    private final GroupEntryCodeGenerator entryCodeGenerator;

    private final GroupMapper groupMapper;

    @Override
    public List<GetGroupTagRes> getGroupTags() {
        return groupReader.getGroupTags().stream().map(groupMapper::toTagDto).toList();
    }

    @Override
    @Transactional
    public Long createGroup(Long userId, CreateGroupReq dto) {
        Group group = groupWorker.create(dto);
        groupMemberWorker.createLeader(userId, group);
        return group.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public GroupEntryCodeRes generateEntryCode(Long userId, Long groupId) {
        authorityValidator.validate(userId);
        Group group = groupFinder.getById(groupId);
        String entryCode = entryCodeGenerator.generateEntryCode(group.getId());

        return new GroupEntryCodeRes(group.getId(), entryCode);
    }
}
