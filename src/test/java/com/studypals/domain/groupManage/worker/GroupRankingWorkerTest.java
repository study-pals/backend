package com.studypals.domain.groupManage.worker;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.groupManage.dao.StudyTimeStatsRepository;
import com.studypals.domain.groupManage.dto.UpdateStudyStatsDto;
import com.studypals.domain.groupManage.entity.GroupRankingPeriod;

@ExtendWith(MockitoExtension.class)
class GroupRankingWorkerTest {

    @InjectMocks
    private GroupRankingWorker groupRankingWorker;

    @Mock
    private StudyTimeStatsRepository studyTimeStatsRepository;

    @Test
    @DisplayName("그룹 랭킹 업데이트 테스트 - 리스트 입력 처리")
    void updateGroupRankings() {
        // given
        Long userId = 1L;
        LocalDate date = LocalDate.of(2025, 1, 1);
        Long studyTime = 3600L;

        // 변경된 시그니처에 맞춰 DTO 리스트 생성
        UpdateStudyStatsDto dto = new UpdateStudyStatsDto(userId, date, studyTime);
        List<UpdateStudyStatsDto> updateStudyStats = List.of(dto);

        // mock: Repository 조회 시 기존 공부 시간 0으로 반환 (String 타입)
        given(studyTimeStatsRepository.findHashFieldsById(anyString(), anyList()))
                .willReturn(Map.of(String.valueOf(userId), "0"));

        // when
        groupRankingWorker.updateGroupRankings(updateStudyStats);

        // then
        // GroupRankingPeriod(일간/주간/월간 등)의 개수만큼 반복해서 저장 로직이 수행되었는지 검증
        // 저장되는 값은 기존 0 + 추가 3600 = 3600
        int periodCount = GroupRankingPeriod.values().length;

        verify(studyTimeStatsRepository, times(periodCount))
                .saveMapById(anyString(), eq(Map.of(String.valueOf(userId), String.valueOf(studyTime))));
    }
}
