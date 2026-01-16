package com.studypals.domain.groupManage.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Slice;
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
import com.studypals.domain.groupManage.worker.*;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.worker.MemberReader;
import com.studypals.domain.studyManage.dto.GroupCategoryDto;
import com.studypals.domain.studyManage.entity.StudyType;
import com.studypals.domain.studyManage.worker.StudyCategoryReader;
import com.studypals.global.exceptions.errorCode.GroupErrorCode;
import com.studypals.global.exceptions.exception.GroupException;
import com.studypals.global.request.Cursor;
import com.studypals.global.responses.CursorResponse;
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
        List<GroupSummaryDto> groups = groupMemberReader.getGroups(userId);
        return assembleGroupResponses(groups);
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
        GroupTotalGoalDto userGoals = groupGoalCalculator.calculateGroupGoals(groupId, profiles);

        // 그룹에 속한 해시태그
        List<String> hashTags = groupHashTagWorker.getHashTagsByGroup(groupId);

        return GetGroupDetailRes.of(group, hashTags, profiles, userGoals);
    }

    @Override
    @Transactional(readOnly = true)
    public CursorResponse.Content<GetGroupsRes> search(GroupSearchDto dto, Cursor cursor) {
        try {
            dto.validate();
        } catch (IllegalArgumentException e) {
            throw new GroupException(
                    GroupErrorCode.GROUP_SEARCH_FAIL,
                    e.getMessage(),
                    "[GroupServiceImpl#search] validate dto fail : " + e.getMessage());
        }

        Slice<Group> groupSlice = groupReader.search(dto, cursor);
        List<Group> groups = groupSlice.getContent();

        if (groups.isEmpty()) {
            return new CursorResponse.Content<>(Collections.emptyList(), 0L, groupSlice.hasNext());
        }

        List<GroupSummaryDto> summary = groups.stream().map(groupMapper::toDto).toList();
        List<GetGroupsRes> responseContent = assembleGroupResponses(summary);

        return new CursorResponse.Content<>(
                responseContent, responseContent.get(responseContent.size() - 1).groupId(), groupSlice.hasNext());
    }

    private List<GetGroupsRes> assembleGroupResponses(List<GroupSummaryDto> groups) {
        List<Long> groupIds = groups.stream().map(GroupSummaryDto::id).toList();

        Map<Long, List<GroupMemberProfileMappingDto>> membersMap = loadTopMembersMap(groupIds);
        Map<Long, List<GroupCategoryDto>> categoriesMap = loadCategoriesMap(groupIds);
        Map<Long, List<String>> hashTagsMap = loadHashTagsMap(groupIds);

        return groups.stream()
                .map(g -> GetGroupsRes.of(
                        g,
                        hashTagsMap.getOrDefault(g.id(), Collections.emptyList()),
                        membersMap.getOrDefault(g.id(), Collections.emptyList()),
                        categoriesMap.getOrDefault(g.id(), Collections.emptyList())))
                .toList();
    }

    private Map<Long, List<GroupMemberProfileMappingDto>> loadTopMembersMap(List<Long> groupIds) {
        List<GroupMemberProfileMappingDto> profileImages = groupMemberReader.getTopNMemberProfileImages(
                groupIds, GroupConst.GROUP_SUMMARY_MEMBER_COUNT.getValue());

        return profileImages.stream().collect(Collectors.groupingBy(GroupMemberProfileMappingDto::groupId));
    }

    private Map<Long, List<GroupCategoryDto>> loadCategoriesMap(List<Long> groupIds) {
        List<GroupCategoryDto> groupCategories =
                studyCategoryReader.findByStudyTypeAndTypeId(StudyType.GROUP, groupIds);

        return groupCategories.stream().collect(Collectors.groupingBy(GroupCategoryDto::groupId));
    }

    private Map<Long, List<String>> loadHashTagsMap(List<Long> groupIds) {
        return groupHashTagWorker.getHashTagsByGroups(groupIds);
    }
}
