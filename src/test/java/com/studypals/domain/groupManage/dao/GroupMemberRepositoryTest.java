package com.studypals.domain.groupManage.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.studypals.domain.groupManage.dto.GroupMemberProfileDto;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupMember;
import com.studypals.domain.groupManage.entity.GroupRole;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.global.config.QueryDslTestConfig;

@DataJpaTest
@DisplayName("GroupMember_QueryDsl_test")
@Import(QueryDslTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class GroupMemberRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    private Member insertMember(String username, String nickname) {
        return em.persist(Member.builder()
                .username(username)
                .password("password")
                .nickname(nickname)
                .imageUrl("image-url")
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

        List<GroupMemberProfileDto> expected = List.of(
                new GroupMemberProfileDto(member1.getImageUrl(), leader.getRole()),
                new GroupMemberProfileDto(member2.getImageUrl(), member.getRole()));

        // when
        List<GroupMemberProfileDto> actual =
                groupMemberRepository.findTopNMember(group.getId(), group.getTotalMember());

        // then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }
}
