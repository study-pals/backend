package com.studypals.domain.groupManage.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.studypals.domain.chatManage.entity.ChatRoom;
import com.studypals.domain.groupManage.dto.GroupMemberProfileDto;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupMember;
import com.studypals.domain.groupManage.entity.GroupRole;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.testModules.testSupport.DataJpaSupport;

@DisplayName("GroupMember_QueryDsl_test")
public class GroupMemberRepositoryTest extends DataJpaSupport {

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    private ChatRoom insertChatRoom(String id) {
        return em.persist(ChatRoom.builder().id(id).name("name" + id).build());
    }

    private Group insertGroup(ChatRoom chatRoom) {
        return em.persist(Group.builder()
                .name("group")
                .tag("tag")
                .totalMember(2)
                .chatRoom(chatRoom)
                .build());
    }

    private GroupMember insertGroupMember(Group group, Member member, GroupRole role) {
        return em.persist(GroupMember.builder()
                .group(group)
                .member(member)
                .role(role)
                .joinedAt(LocalDate.now())
                .build());
    }

    @Test
    void findTopNMembers_success() {
        // given
        Member member1 = insertMember("username1", "member1");
        Member member2 = insertMember("username2", "member2");
        ChatRoom chatRoom = insertChatRoom("chatRoom");
        Group group = insertGroup(chatRoom);
        GroupMember leader = insertGroupMember(group, member1, GroupRole.LEADER);
        GroupMember member = insertGroupMember(group, member2, GroupRole.MEMBER);

        List<GroupMemberProfileDto> expected = List.of(
                new GroupMemberProfileDto(
                        member1.getId(), member1.getNickname(), member1.getImageUrl(), leader.getRole()),
                new GroupMemberProfileDto(
                        member2.getId(), member2.getNickname(), member2.getImageUrl(), member.getRole()));

        // when
        List<GroupMemberProfileDto> actual =
                groupMemberRepository.findTopNMemberByJoinedAt(group.getId(), group.getTotalMember());

        // then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }
}
