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
import com.studypals.domain.groupManage.dto.GroupMemberProfileDto;
import com.studypals.domain.groupManage.dto.GroupMemberRankingDto;
import com.studypals.domain.groupManage.entity.GroupRankingPeriod;
import com.studypals.domain.groupManage.entity.GroupRole;
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
    @DisplayName("그룹 랭킹 조회 시 본인의 닉네임은 '사용자'로 변경되고, 데이터가 없으면 0으로 초기화된다")
    void getGroupRanking_Transformation_Success() {
        // given
        Long myId = 1L;
        Long otherId = 2L;
        LocalDate now = LocalDate.of(2025, 12, 26);
        GroupRankingPeriod period = GroupRankingPeriod.WEEKLY;

        List<GroupMemberProfileDto> profiles = List.of(
                new GroupMemberProfileDto(myId, "내닉네임", "my-url", GroupRole.LEADER),
                new GroupMemberProfileDto(otherId, "친구닉네임", "other-url", GroupRole.MEMBER));

        given(timeUtils.getToday()).willReturn(now);
        // Redis 결과 Mocking: 내 데이터는 5000초, 친구 데이터는 없음(null 상황 가정)
        given(groupRankingRepository.getGroupRanking(eq(now), anyList(), eq(period)))
                .willReturn(Map.of(String.valueOf(myId), "5000"));

        // when
        List<GroupMemberRankingDto> result = groupRankingWorker.getGroupRanking(myId, profiles, period);

        // then
        assertThat(result).hasSize(2);

        // 내 정보 검증 ("사용자" 치환 및 시간 확인)
        GroupMemberRankingDto myRanking =
                result.stream().filter(r -> r.id().equals(myId)).findFirst().get();
        assertThat(myRanking.nickname()).isEqualTo("사용자");
        assertThat(myRanking.studyTime()).isEqualTo(5000L);

        // 친구 정보 검증 (기존 닉네임 유지 및 0초 처리)
        GroupMemberRankingDto otherRanking =
                result.stream().filter(r -> r.id().equals(otherId)).findFirst().get();
        assertThat(otherRanking.nickname()).isEqualTo("친구닉네임");
        assertThat(otherRanking.studyTime()).isEqualTo(0L);
    }
}
