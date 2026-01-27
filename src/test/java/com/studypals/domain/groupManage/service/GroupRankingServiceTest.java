package com.studypals.domain.groupManage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.groupManage.dto.GroupMemberRankingDto;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupMember;
import com.studypals.domain.groupManage.entity.GroupRankingPeriod;
import com.studypals.domain.groupManage.entity.GroupRole;
import com.studypals.domain.groupManage.worker.GroupAuthorityValidator;
import com.studypals.domain.groupManage.worker.GroupMemberReader;
import com.studypals.domain.groupManage.worker.GroupRankingWorker;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.global.file.ObjectStorage;

@ExtendWith(MockitoExtension.class)
class GroupRankingServiceTest {

    @Mock
    private GroupRankingWorker groupRankingWorker;

    @Mock
    private GroupMemberReader groupMemberReader;

    @Mock
    private GroupAuthorityValidator validator;

    @Mock
    private ObjectStorage objectStorage;

    @InjectMocks
    private GroupRankingServiceImpl groupRankingService;

    @Test
    @DisplayName("그룹 ID를 통해 멤버 프로필을 조회한 후 랭킹 계산 Worker를 호출해야 한다")
    void getGroupRanking_Success() {
        // given
        Long userId = 1L;
        Long groupId = 10L;
        GroupRankingPeriod period = GroupRankingPeriod.DAILY;

        List<GroupMember> groupMembers = createMockGroupMembers(groupId);

        Map<Long, Long> expectedResponse = Map.of(1L, 10000L, 2L, 20000L, 3L, 5000L, 4L, 0L);

        // Reader와 Worker의 행위 정의
        given(groupMemberReader.getAllMemberProfiles(groupId)).willReturn(groupMembers);
        given(groupRankingWorker.getGroupRanking(groupMembers, period)).willReturn(expectedResponse);

        // when
        List<GroupMemberRankingDto> result = groupRankingService.getGroupRanking(userId, groupId, period);

        // then
        assertThat(result).hasSize(4);
        assertThat(result.get(0).nickname()).isEqualTo("개발자A");
        assertThat(result.get(1).nickname()).isEqualTo("열공학생B");
        assertThat(result.get(2).nickname()).isEqualTo("스터디봇C");
        assertThat(result.get(3).nickname()).isEqualTo("코딩천재D");

        // 의존성 객체들이 정해진 파라미터로 호출되었는지 검증
        verify(groupMemberReader).getAllMemberProfiles(groupId);
        verify(groupRankingWorker).getGroupRanking(groupMembers, period);
    }

    // 헬퍼 메서드: GroupMember 엔티티 4명 생성
    private List<GroupMember> createMockGroupMembers(Long groupId) {
        Group group = Group.builder().id(groupId).build();

        return List.of(
                createMember(1L, "개발자A", "img_a", group, GroupRole.LEADER),
                createMember(2L, "열공학생B", "img_b", group, GroupRole.MEMBER),
                createMember(3L, "스터디봇C", "img_c", group, GroupRole.MEMBER),
                createMember(4L, "코딩천재D", "img_d", group, GroupRole.MEMBER));
    }

    private GroupMember createMember(Long id, String nick, String img, Group group, GroupRole role) {
        Member member = Member.builder().id(id).nickname(nick).build();

        return GroupMember.builder()
                .id(id + 1000L) // GroupMember 자체의 ID
                .member(member)
                .group(group)
                .role(role)
                .joinedAt(LocalDate.now())
                .build();
    }
}
