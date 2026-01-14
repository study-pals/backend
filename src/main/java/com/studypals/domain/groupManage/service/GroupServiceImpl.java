package com.studypals.domain.groupManage.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.dto.CreateChatRoomDto;
import com.studypals.domain.chatManage.entity.ChatRoom;
import com.studypals.domain.chatManage.worker.ChatRoomWriter;
import com.studypals.domain.groupManage.dto.*;
import com.studypals.domain.groupManage.dto.mappers.GroupMapper;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupConst;
import com.studypals.domain.groupManage.entity.GroupMember;
import com.studypals.domain.groupManage.worker.*;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.worker.MemberReader;
import com.studypals.domain.studyManage.dto.GroupCategoryDto;
import com.studypals.domain.studyManage.entity.StudyType;
import com.studypals.domain.studyManage.worker.StudyCategoryReader;
import com.studypals.global.retry.RetryTx;

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

    private final GroupHashTagWorker groupHashTagWorker;

    // chat room worker class
    private final ChatRoomWriter chatRoomWriter;
    private final StudyCategoryReader studyCategoryReader;

    @Override
    public List<GetGroupTagRes> getGroupTags() {
        return groupReader.getGroupTags().stream().map(groupMapper::toTagDto).toList();
    }

    @Override
    @RetryTx(
            maxAttempts = 2,
            retryFor = {DataIntegrityViolationException.class})
    public Long createGroup(Long userId, CreateGroupReq dto) {
        // 그룹 생성
        Group group = groupWriter.create(dto);
        Member member = memberReader.getRef(userId);
        groupMemberWriter.createLeader(member, group);

        groupHashTagWorker.saveTags(group, dto.hashTags());

        // 채팅방 생성
        CreateChatRoomDto createChatRoomDto = new CreateChatRoomDto(dto.name(), dto.imageUrl());
        ChatRoom chatRoom = chatRoomWriter.create(createChatRoomDto);
        chatRoomWriter.joinAsAdmin(chatRoom, member);
        group.setChatRoom(chatRoom);

        return group.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetGroupsRes> getGroups(Long userId) {
        // jwt filter 에서 주입한 userId이므로 DB에 존재하는지 체크하지 않음
        List<GroupSummaryDto> groups = groupMemberReader.getGroups(userId);

        List<Long> groupIds = groups.stream().map(GroupSummaryDto::id).toList();

        // 각 그룹에 속한 멤버들 프로필, 역할 조회하기
        List<GroupMemberProfileMappingDto> profileImages = groupMemberReader.getTopNMemberProfileImages(
                groupIds, GroupConst.GROUP_SUMMARY_MEMBER_COUNT.getValue());

        // 그룹id : 속한 멤버들
        Map<Long, List<GroupMemberProfileMappingDto>> membersMap =
                profileImages.stream().collect(Collectors.groupingBy(GroupMemberProfileMappingDto::groupId));

        // 각 그룹이 가지고 있는 카테고리 조회하기
        List<GroupCategoryDto> groupCategories =
                studyCategoryReader.findByStudyTypeAndTypeId(StudyType.GROUP, groupIds);

        // 그룹id : 속한 카테고리들
        Map<Long, List<GroupCategoryDto>> categoriesMap =
                groupCategories.stream().collect(Collectors.groupingBy(GroupCategoryDto::groupId));

        return groups.stream()
                .map(group -> GetGroupsRes.of(
                        group,
                        membersMap.getOrDefault(group.id(), Collections.emptyList()),
                        categoriesMap.getOrDefault(group.id(), Collections.emptyList())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GetGroupDetailRes getGroupDetails(Long userId, Long groupId) {
        // 해당 유저가 속한 그룹인가?
        validator.isMemberOfGroup(userId, groupId);

        Group group = groupReader.getById(groupId);

        List<GroupMember> groupMembers = groupMemberReader.getAllMemberProfiles(group.getId());

        // 그룹에 속한 유저들의 목표 달성률 계산
        GroupTotalGoalDto userGoals = groupGoalCalculator.calculateGroupGoals(groupId, groupMembers);

        return GetGroupDetailRes.of(group, groupMembers, userGoals);
    }

    @Override
    @Transactional
    public Long updateGroup(Long userId, Long groupId, UpdateGroupReq dto) {
        Group group = groupReader.getById(groupId);

        groupWriter.update(userId, groupId, group, dto);

        return group.getId();
    }
}
