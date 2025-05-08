package com.studypals.domain.studyManage.worker;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.dao.StudyTimeRepository;
import com.studypals.domain.studyManage.entity.StudyStatus;
import com.studypals.domain.studyManage.entity.StudyTime;
import com.studypals.domain.studyManage.worker.strategy.StudyTimePersistenceStrategy;
import com.studypals.domain.studyManage.worker.strategy.StudyTimePersistenceStrategyFactory;

/**
 * {@link StudySessionWorker} 에 대한 테스트
 *
 * @author jack8
 * @since 2025-04-19
 */
@ExtendWith(MockitoExtension.class)
class StudySessionWorkerTest {

    @Mock
    private StudyTimeRepository studyTimeRepository;

    @Mock
    private StudyTimePersistenceStrategyFactory strategyFactory;

    @Mock
    private StudyTimePersistenceStrategy strategy;

    @Mock
    private Member mockMember;

    @Mock
    private StudyStatus mockStatus;

    @Mock
    private StudyTime mockStudyTime;

    @InjectMocks
    private StudySessionWorker studySessionWorker;

    @Test
    void upsert_success_withCategory_alreadyExistStudyTime() {
        // given
        Long userId = 1L;
        LocalDate date = LocalDate.of(2025, 3, 1);
        Long time = 300L;

        given(strategyFactory.resolve(mockStatus)).willReturn(strategy);
        given(strategy.find(mockMember, mockStatus, date)).willReturn(Optional.of(mockStudyTime));

        // when
        studySessionWorker.upsert(mockMember, mockStatus, date, time);

        // then
        then(mockStudyTime).should().addTime(time);
        then(mockMember).should().addToken(time / 60);
    }

    @Test
    void upsert_success_withCategory_firstSaveStudyTime() {
        // given
        Long userId = 1L;
        LocalDate date = LocalDate.of(2025, 3, 1);
        Long time = 300L;

        given(strategyFactory.resolve(mockStatus)).willReturn(strategy);
        given(strategy.find(mockMember, mockStatus, date)).willReturn(Optional.empty());

        // when
        studySessionWorker.upsert(mockMember, mockStatus, date, time);

        // then
        then(strategy).should().create(mockMember, mockStatus, date, time);
        then(mockMember).should().addToken(time / 60);
    }
}
