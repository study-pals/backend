package com.studypals.domain.groupManage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.groupManage.dto.GroupMemberProfileDto;
import com.studypals.domain.groupManage.dto.GroupMemberRankingDto;
import com.studypals.domain.groupManage.entity.GroupRankingPeriod;
import com.studypals.domain.groupManage.entity.GroupRole;
import com.studypals.domain.groupManage.worker.GroupMemberReader;
import com.studypals.domain.groupManage.worker.GroupRankingWorker;

@ExtendWith(MockitoExtension.class)
class GroupRankingServiceImplTest {

    @Mock
    private GroupRankingWorker groupRankingWorker;

    @Mock
    private GroupMemberReader groupMemberReader;

    @InjectMocks
    private GroupRankingServiceImpl groupRankingService;

    @Test
    @DisplayName("그룹 ID를 통해 멤버 프로필을 조회한 후 랭킹 계산 Worker를 호출해야 한다")
    void getGroupRanking_Success() {
        // given
        Long userId = 1L;
        Long groupId = 10L;
        GroupRankingPeriod period = GroupRankingPeriod.DAILY;

        List<GroupMemberProfileDto> mockProfiles = List.of(
                new GroupMemberProfileDto(1L, "유저1", "url1", GroupRole.LEADER),
                new GroupMemberProfileDto(2L, "유저2", "url2", GroupRole.MEMBER));

        List<GroupMemberRankingDto> expectedResponse = List.of(
                new GroupMemberRankingDto(1L, "사용자", "url1", 3600L, GroupRole.LEADER),
                new GroupMemberRankingDto(2L, "유저2", "url2", 1800L, GroupRole.MEMBER));

        // Reader와 Worker의 행위 정의
        given(groupMemberReader.getAllMemberProfiles(groupId)).willReturn(mockProfiles);
        given(groupRankingWorker.getGroupRanking(userId, mockProfiles, period)).willReturn(expectedResponse);

        // when
        List<GroupMemberRankingDto> result = groupRankingService.getGroupRanking(userId, groupId, period);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).nickname()).isEqualTo("사용자");

        // 의존성 객체들이 정해진 파라미터로 호출되었는지 검증
        verify(groupMemberReader).getAllMemberProfiles(groupId);
        verify(groupRankingWorker).getGroupRanking(userId, mockProfiles, period);
    }
}
