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
    private final GroupMemberWriter groupMemberWriter;
    private final GroupMapper groupMapper;

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
}
