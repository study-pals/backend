package com.studypals.domain.groupManage.worker;

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

import com.studypals.domain.groupManage.dao.GroupRankingRepository;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupMember;
import com.studypals.domain.groupManage.entity.GroupRankingPeriod;
import com.studypals.domain.groupManage.entity.GroupRole;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.global.utils.TimeUtils;

@ExtendWith(MockitoExtension.class)
class GroupRankingWorkerTest {

    @Mock
    private GroupRankingRepository groupRankingRepository;

    @Mock
    private TimeUtils timeUtils;

    @InjectMocks
    private GroupRankingWorker groupRankingWorker;

    @Test
    @DisplayName("공부 시간 업데이트 시 Repository의 증가 메서드가 호출되어야 한다")
    void updateGroupRankings_Success() {
        // given
        Long userId = 1L;
        Long studyTime = 3600L;
        LocalDate now = LocalDate.of(2025, 12, 26);
        given(timeUtils.getToday()).willReturn(now);

        // when
        groupRankingWorker.updateGroupRankings(userId, studyTime);

        // then
        verify(groupRankingRepository, times(1)).incrementUserStudyTime(now, userId, studyTime);
    }

    @Test
    @DisplayName("그룹 랭킹 조회 시 공부 데이터가 없으면 0으로 반환한다")
    void getGroupRanking_Transformation_Success() {
        // given
        Long myId = 1L;
        Long otherId = 2L;
        LocalDate now = LocalDate.of(2025, 12, 26);
        GroupRankingPeriod period = GroupRankingPeriod.WEEKLY;

        List<GroupMember> profiles = createMockGroupMembers(1L);

        given(timeUtils.getToday()).willReturn(now);
        // Redis 결과 Mocking: 내 데이터는 5000초, 친구 데이터는 없음(null 상황 가정)
        given(groupRankingRepository.getGroupRanking(eq(now), anyList(), eq(period)))
                .willReturn(Map.of(1L, 10000L, 2L, 20000L, 3L, 5000L, 4L, 0L));

        // when
        Map<Long, Long> result = groupRankingWorker.getGroupRanking(profiles, period);

        // then
        assertThat(result).hasSize(4);
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
        Member member = Member.builder()
                .id(id)
                .nickname(nick)
                .imageUrl("https://example.com/" + img)
                .build();

        return GroupMember.builder()
                .id(id + 1000L) // GroupMember 자체의 ID
                .member(member)
                .group(group)
                .role(role)
                .joinedAt(LocalDate.now())
                .build();
    }
}
