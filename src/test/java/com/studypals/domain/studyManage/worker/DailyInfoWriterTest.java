package com.studypals.domain.studyManage.worker;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.dao.DailyStudyInfoRepository;
import com.studypals.domain.studyManage.entity.DailyStudyInfo;

/**
 * {@link DailyInfoWriter} 에 대한 테스트
 *
 * @author jack8
 * @since 2025-05-07
 */
@ExtendWith(MockitoExtension.class)
class DailyInfoWriterTest {

    @Mock
    private DailyStudyInfoRepository dailyStudyInfoRepository;

    @Mock
    private Member mockMember;

    @Mock
    private DailyStudyInfo mockDailyStudyInfo;

    @InjectMocks
    private DailyInfoWriter dailyInfoWriter;

    @Test
    void updateEndTime_success() {
        // given
        Long userId = 1L;
        LocalDate date = LocalDate.of(2024, 3, 1);
        LocalTime endTime = LocalTime.of(12, 0, 0);

        given(mockMember.getId()).willReturn(userId);
        given(dailyStudyInfoRepository.findByMemberIdAndStudiedDate(userId, date))
                .willReturn(Optional.of(mockDailyStudyInfo));

        // when
        dailyInfoWriter.updateEndtime(mockMember, date, endTime);

        // then
        then(mockDailyStudyInfo).should().setEndTime(endTime);
    }
}
