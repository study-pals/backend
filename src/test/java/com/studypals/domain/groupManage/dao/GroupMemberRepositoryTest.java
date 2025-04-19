package com.studypals.domain.groupManage.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.studypals.domain.groupManage.dto.GroupMemberProfileImageDto;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupMember;
import com.studypals.domain.groupManage.entity.GroupRole;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.testModules.testSupport.DataJpaSupport;

@DisplayName("GroupMember_QueryDsl_test")
public class GroupMemberRepositoryTest extends DataJpaSupport {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    private Member insertMember(String username, String nickname) {
        return em.persist(Member.builder()
                .username(username)
                .password("password")
                .nickname(nickname)
                .imageUrl("imageUrl-url")
                .build());
    }

    private Group insertGroup() {
        return em.persist(
                Group.builder().name("group").tag("tag").totalMember(2).build());
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
        Group group = insertGroup();
        GroupMember leader = insertGroupMember(group, member1, GroupRole.LEADER);
        GroupMember member = insertGroupMember(group, member2, GroupRole.MEMBER);

        List<GroupMemberProfileImageDto> expected = List.of(
                new GroupMemberProfileImageDto(member1.getImageUrl(), leader.getRole()),
                new GroupMemberProfileImageDto(member2.getImageUrl(), member.getRole()));

        // when
        List<GroupMemberProfileImageDto> actual =
                groupMemberRepository.findTopNMemberByJoinedAt(group.getId(), group.getTotalMember());

        // then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }
}
