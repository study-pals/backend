package com.studypals.domain.groupManage.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dto.*;
import com.studypals.domain.groupManage.dto.mappers.GroupMapper;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.worker.*;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.worker.MemberReader;

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
    private static final int GROUP_SUMMARY_MEMBER_COUNT = 5;

    private final MemberReader memberReader;
    private final GroupWorker groupWorker;
    private final GroupReader groupReader;
    private final GroupMemberWorker groupMemberWorker;
    private final GroupMemberReader groupMemberReader;

    private final GroupAuthorityValidator authorityValidator;
    private final GroupEntryCodeManager entryCodeManager;

    private final GroupMapper groupMapper;

    @Override
    public List<GetGroupTagRes> getGroupTags() {
        return groupReader.getGroupTags().stream().map(groupMapper::toTagDto).toList();
    }

    @Override
    @Transactional
    public Long createGroup(Long userId, CreateGroupReq dto) {
        Group group = groupWorker.create(dto);
        Member member = memberReader.getRef(userId);
        groupMemberWorker.createLeader(member, group);
        return group.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public GroupEntryCodeRes generateEntryCode(Long userId, Long groupId) {
        Group group = groupReader.getById(groupId);
        authorityValidator.validate(userId);
        String entryCode = entryCodeManager.generate(group.getId());

        return new GroupEntryCodeRes(group.getId(), entryCode);
    }

    @Override
    @Transactional(readOnly = true)
    public GroupSummaryRes getGroupSummary(String entryCode) {
        Long groupId = entryCodeManager.getGroupId(entryCode);
        Group group = groupReader.getById(groupId);
        List<GroupMemberProfileDto> profiles =
                groupMemberReader.getTopNMemberProfiles(groupId, GROUP_SUMMARY_MEMBER_COUNT);

        return GroupSummaryRes.of(group, profiles);
    }
}
