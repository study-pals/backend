package com.studypals.domain.groupManage.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.groupManage.dao.StudyTimeStatsRepository;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupMember;
import com.studypals.domain.groupManage.entity.GroupRankingPeriod;
import com.studypals.domain.groupManage.entity.GroupRole;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.global.utils.TimeUtils;

@ExtendWith(MockitoExtension.class)
class GroupRankingWorkerTest {

    @Mock
    private StudyTimeStatsRepository studyTimeStatsRepository;

    @Mock
    private TimeUtils timeUtils;

    @InjectMocks
    private GroupRankingWorker groupRankingWorker;

    @Test
    @DisplayName("공부 시간 업데이트 시 일/주/월 키에 대해 각각 조회 후 저장이 발생해야 한다")
    void updateGroupRankings_Success() {
        // given
        Long userId = 1L;
        String userIdStr = "1";
        Long addTime = 3600L;
        LocalDate now = LocalDate.of(2025, 12, 26);

        given(timeUtils.getToday()).willReturn(now);

        // 각 기간별(일/주/월) Redis Key 생성 (실제 Enum 로직과 동일하게 설정 필요)
        List<String> expectedKeys = Arrays.stream(GroupRankingPeriod.values())
                .map(period -> period.getRedisKey(now))
                .toList();

        // findHashFieldsById 호출 시 기존 값이 1000L이었다고 가정
        for (String key : expectedKeys) {
            given(studyTimeStatsRepository.findHashFieldsById(key, List.of(userIdStr)))
                    .willReturn(Map.of(userIdStr, "1000"));
        }

        // when
        groupRankingWorker.updateGroupRankings(userId, addTime);

        // then
        for (String key : expectedKeys) {
            // 1. 조회가 각각 일어났는지 확인
            verify(studyTimeStatsRepository).findHashFieldsById(key, List.of(userIdStr));

            // 2. 기존 1000 + 추가 3600 = "4600"이 저장되었는지 확인
            verify(studyTimeStatsRepository).saveMapById(key, Map.of(userIdStr, "4600"));
        }
    }

    @Test
    @DisplayName("그룹 랭킹 조회 시 Redis 데이터를 Long으로 변환한다")
    void getGroupRanking_Transformation_Success() {
        // 1. Given
        LocalDate now = LocalDate.of(2025, 12, 26);
        GroupRankingPeriod period = GroupRankingPeriod.WEEKLY;
        String expectedKey = "weekly:2025W52"; // period.getRedisKey(now)의 결과와 일치해야 함

        // Mock 멤버 리스트 생성 (ID 1, 2, 3, 4)
        List<GroupMember> profiles = createMockGroupMembers(1L);

        // TimeUtils가 'now'를 반환하도록 설정
        given(timeUtils.getToday()).willReturn(now);

        // [중요] Repository 스터빙
        // anyString()과 anyList()를 사용하여 인자 불일치 에러를 방지하거나, 정확한 값을 eq()로 지정
        Map<String, String> mockRedisData = Map.of(
                "1", "1000",
                "2", "2000",
                "3", "3000",
                "4", "4000");

        // 위 로그에서 [1, 2, 3, 4]가 호출된다고 했으므로 anyList() 혹은 eq(List.of("1","2","3","4")) 사용
        given(studyTimeStatsRepository.findHashFieldsById(eq(expectedKey), anyList()))
                .willReturn(mockRedisData);

        // 2. When
        Map<Long, Long> result = groupRankingWorker.getGroupRanking(profiles, period);

        // 3. Then
        assertThat(result).hasSize(4);
        assertThat(result.get(1L)).isEqualTo(1000L);
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
