package com.studypals.domain.groupManage.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.dto.CreateChatRoomDto;
import com.studypals.domain.chatManage.entity.ChatRoom;
import com.studypals.domain.chatManage.worker.ChatRoomWriter;
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
    private final MemberReader memberReader;
    private final GroupWriter groupWriter;
    private final GroupReader groupReader;
    private final GroupMemberReader groupMemberReader;
    private final GroupMemberWriter groupMemberWriter;
    private final GroupAuthorityValidator validator;
    private final GroupMapper groupMapper;
    private final GroupGoalCalculator groupGoalCalculator;

    // chat room worker class
    private final ChatRoomWriter chatRoomWriter;

    @Override
    public List<GetGroupTagRes> getGroupTags() {
        return groupReader.getGroupTags().stream().map(groupMapper::toTagDto).toList();
    }

    @Override
    @Transactional
    public Long createGroup(Long userId, CreateGroupReq dto) {
        // 그룹 생성
        Group group = groupWriter.create(dto);
        Member member = memberReader.getRef(userId);
        groupMemberWriter.createLeader(member, group);

        // 채팅방 생성
        CreateChatRoomDto createChatRoomDto = new CreateChatRoomDto(dto.name());
        ChatRoom chatRoom = chatRoomWriter.create(createChatRoomDto);
        chatRoomWriter.joinAsAdmin(chatRoom, member);
        group.setChatRoom(chatRoom);

        return group.getId();
    }

    @Override
    @Transactional(readOnly = true) // 붙이는게 이득일까?
    public List<GetGroupsRes> getGroups(Long userId) {
        // 유효한 userId인지 검사
        memberReader.get(userId);

        List<GroupSummaryDto> groups = groupMemberReader.getGroups(userId);

        return groups.stream().map(GetGroupsRes::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GetGroupDetailRes getGroupDetails(Long userId, Long groupId) {
        // 해당 유저가 속한 그룹인가?
        validator.isMemberOfGroup(userId, groupId);

        Group group = groupReader.getById(groupId);

        // 그룹에 속한 유저들 프로필
        List<GroupMemberProfileDto> profiles = groupMemberReader.getAllMemberProfiles(group);

        // 그룹에 속한 유저들의 목표 달성률 계산
        List<GroupCategoryGoalDto> userGoals = groupGoalCalculator.calculateGroupGoals(groupId, profiles);

        return GetGroupDetailRes.of(group, profiles, userGoals);
    }
}
