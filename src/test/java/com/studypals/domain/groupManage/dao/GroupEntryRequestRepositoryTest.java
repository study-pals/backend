package com.studypals.domain.groupManage.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupEntryRequest;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.global.request.CommonSortType;
import com.studypals.global.request.Cursor;
import com.studypals.testModules.testSupport.DataJpaSupport;

@DisplayName("GroupEntryRequest_Querydsl_test")
public class GroupEntryRequestRepositoryTest extends DataJpaSupport {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private GroupEntryRequestRepository entryRequestRepository;

    private Member insertMember(String username, String nickname) {
        return em.persist(Member.builder()
                .username(username)
                .password("password")
                .nickname(nickname)
                .imageUrl("imageUrl-url")
                .build());
    }

    private Group insertGroup() {
        return em.persist(Group.builder().name("group").tag("tag").build());
    }

    private GroupEntryRequest insertRequest(Member member, Group group) {
        return em.persist(
                GroupEntryRequest.builder().member(member).group(group).build());
    }

    @Test
    void findByGroupIdAndSortBy_success() {
        // given
        Member member1 = insertMember("member1", "member1");
        Member member2 = insertMember("member2", "member2");
        Group group = insertGroup();
        GroupEntryRequest request1 = insertRequest(member1, group);
        GroupEntryRequest request2 = insertRequest(member2, group);

        Slice<GroupEntryRequest> expected = new SliceImpl<>(List.of(request1, request2));

        // when
        Cursor cursor = new Cursor(0, 10, CommonSortType.NEW);
        Slice<GroupEntryRequest> actual = entryRequestRepository.findByGroupIdAndSortBy(group.getId(), cursor);

        // then
        assertThat(actual.getContent()).containsExactlyElementsOf(expected.getContent());
        assertThat(actual.hasNext()).isEqualTo(expected.hasNext());
    }
}
