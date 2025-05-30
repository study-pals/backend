package com.studypals.domain.groupManage.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.worker.ChatRoomWriter;
import com.studypals.domain.groupManage.dto.*;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupEntryRequest;
import com.studypals.domain.groupManage.worker.*;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.worker.MemberReader;
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
    /** 그룹 요약 정보 조회 시 포함되는 그룹 멤버 수 */
    private static final int GROUP_SUMMARY_MEMBER_COUNT = 5;

    private final MemberReader memberReader;
    private final GroupReader groupReader;
    private final GroupMemberWriter groupMemberWriter;
    private final GroupMemberReader groupMemberReader;

    private final GroupAuthorityValidator authorityValidator;
    private final GroupEntryCodeManager entryCodeManager;
    private final GroupEntryRequestReader entryRequestReader;
    private final GroupEntryRequestWriter entryRequestWriter;

    private final ChatRoomWriter chatRoomWriter;

    @Override
    @Transactional(readOnly = true)
    public GroupEntryCodeRes generateEntryCode(Long userId, Long groupId) {
        authorityValidator.validate(userId, groupId);
        String entryCode = entryCodeManager.generate(groupId);

        return new GroupEntryCodeRes(groupId, entryCode);
    }

    @Override
    @Transactional(readOnly = true)
    public GroupSummaryRes getGroupSummary(String entryCode) {
        Long groupId = entryCodeManager.getGroupId(entryCode);
        Group group = groupReader.getById(groupId);
        List<GroupMemberProfileDto> profiles =
                groupMemberReader.getTopNMemberProfiles(group, GROUP_SUMMARY_MEMBER_COUNT);

        return GroupSummaryRes.of(group, profiles);
    }

    @Override
    @Transactional
    public Long joinGroup(Long userId, GroupEntryReq entryInfo) {
        Group group = groupReader.getById(entryInfo.groupId());
        if (group.isApprovalRequired()) {
            throw new GroupException(GroupErrorCode.GROUP_JOIN_FAIL, "can't join without permission");
        }

        entryCodeManager.validateCodeBelongsToGroup(group, entryInfo.entryCode());
        Member member = memberReader.getRef(userId);
        return internalJoinGroup(member, group);
    }

    @Override
    @Transactional
    public Long requestParticipant(Long userId, GroupEntryReq entryInfo) {
        Group group = groupReader.getById(entryInfo.groupId());
        entryRequestWriter.validateNewRequestAvailable(group);
        entryCodeManager.validateCodeBelongsToGroup(group, entryInfo.entryCode());
        Member member = memberReader.getRef(userId);
        return entryRequestWriter.createRequest(member, group).getId();
    }

    @Override
    @Transactional
    public Long acceptEntryRequest(Long userId, AcceptEntryReq req) {
        authorityValidator.validate(userId, req.groupId());
        GroupEntryRequest request = entryRequestReader.getById(req.requestId());
        entryRequestWriter.closeRequest(request);

        return internalJoinGroup(request.getMember(), request.getGroup());
    }

    @Override
    @Transactional
    public void refuseEntryRequest(Long userId, Long requestId) {
        GroupEntryRequest request = entryRequestReader.getById(requestId);
        authorityValidator.validate(userId, request.getGroup().getId());
        entryRequestWriter.closeRequest(request);
    }

    // 그룹 참여 시 공통 로직을 private 으로 분리
    private Long internalJoinGroup(Member member, Group group) {
        Long joinId = groupMemberWriter.createMember(member, group).getId();

        chatRoomWriter.join(group.getChatRoom(), member);

        return joinId;
    }
}
