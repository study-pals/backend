package com.studypals.domain.groupManage.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.studypals.domain.chatManage.entity.ChatRoom;
import com.studypals.domain.groupManage.dto.GroupMemberProfileDto;
import com.studypals.domain.groupManage.dto.GroupMemberProfileMappingDto;
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
        return em.persist(
                ChatRoom.builder().id(id).name("name" + id).totalMember(1).build());
    }

    private Group insertGroup(ChatRoom chatRoom) {
        return em.persist(Group.builder()
                .totalMember(2)
                .name("group")
                .tag("tag")
                .maxMember(10)
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

    @Test
    void findAllMemberProfiles_success() {
        // given
        Member m1 = insertMember("user1", "리더");
        Member m2 = insertMember("user2", "멤버");
        ChatRoom chatRoom = insertChatRoom("chat1");
        Group group = insertGroup(chatRoom);

        insertGroupMember(group, m2, GroupRole.MEMBER); // 나중에 가입한 일반 멤버
        insertGroupMember(group, m1, GroupRole.LEADER); // 리더

        // when
        List<GroupMember> result = groupMemberRepository.findGroupMembers(group.getId());

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMember().getNickname()).isEqualTo("리더"); // 리더가 우선순위로 나와야 함
        assertThat(result.get(0).getRole()).isEqualTo(GroupRole.LEADER);
    }

    @Test
    void findAllMembersInGroupIds_success() {
        // given
        int limit = 4;
        Member m1 = insertMember("user1", "그룹1리더");
        Member m2 = insertMember("user2", "그룹2리더");

        ChatRoom cr1 = insertChatRoom("chat1");
        ChatRoom cr2 = insertChatRoom("chat2");
        Group g1 = insertGroup(cr1);
        Group g2 = insertGroup(cr2);

        insertGroupMember(g1, m1, GroupRole.LEADER);
        insertGroupMember(g2, m2, GroupRole.LEADER);

        List<Long> groupIds = List.of(g1.getId(), g2.getId());

        // when
        List<GroupMemberProfileMappingDto> result = groupMemberRepository.findTopNMemberInGroupIds(groupIds, limit);

        // then
        assertThat(result).hasSize(2);

        // MappingDto에 groupId가 정확히 매칭되었는지 검증 (가장 중요)
        GroupMemberProfileMappingDto mapping1 = result.stream()
                .filter(r -> r.groupId().equals(g1.getId()))
                .findFirst()
                .get();
        assertThat(mapping1.groupId()).isEqualTo(g1.getId());
        assertThat(mapping1.imageUrl()).isEqualTo("imageUrl-url");

        GroupMemberProfileMappingDto mapping2 = result.stream()
                .filter(r -> r.groupId().equals(g2.getId()))
                .findFirst()
                .get();
        assertThat(mapping2.groupId()).isEqualTo(g2.getId());
        assertThat(mapping2.imageUrl()).isEqualTo("imageUrl-url");
    }
}
