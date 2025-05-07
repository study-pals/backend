package com.studypals.domain.studyManage.worker;

import java.time.LocalDate;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.dao.StudyTimeRepository;
import com.studypals.domain.studyManage.entity.StudyStatus;
import com.studypals.domain.studyManage.entity.StudyTime;
import com.studypals.domain.studyManage.worker.strategy.StudyTimePersistenceStrategy;
import com.studypals.domain.studyManage.worker.strategy.StudyTimePersistenceStrategyFactory;
import com.studypals.global.annotations.Worker;
import com.studypals.global.exceptions.errorCode.StudyErrorCode;
import com.studypals.global.exceptions.exception.StudyException;

/**
 * service - repository 사이의 worker 계층입니다. 공부가 시작-종료 됨에 따라
 * 발생하는 로직들을 담당합니다.
 *
 * @author jack8
 * @since 2025-04-15
 */
@Worker
@RequiredArgsConstructor
public class StudySessionWorker {

    private final StudyTimeRepository studyTimeRepository;
    private final StudyTimePersistenceStrategyFactory strategyFactory;

    private static final Long TOKEN_CALCULATE_VALUE = 60L;

    /**
     * studyTime 을 최신화하는 메서드. category에 대한 공부인지, temporaryName 에 대한 공부인지에 따라
     * 갈린다.
     * @param status redis 에 저장된 사용자 상태
     * @param studiedDate 언제 공부했는지에 대한 날짜(today)
     * @param time 초 단위 공부 시간
     */
    public void upsert(Member member, StudyStatus status, LocalDate studiedDate, Long time) {

        member.addToken(calculateToken(time));

        StudyTimePersistenceStrategy strategy = strategyFactory.resolve(status);

        strategy.find(member, status, studiedDate)
                .ifPresentOrElse(
                        st -> st.addTime(time), () -> saveTime(strategy.create(member, status, studiedDate, time)));
    }

    private void saveTime(StudyTime time) {
        try {
            studyTimeRepository.save(time);
        } catch (Exception e) {
            throw new StudyException(StudyErrorCode.STUDY_TIME_END_FAIL, "save fail");
        }
    }

    private Long calculateToken(Long time) {
        return time / TOKEN_CALCULATE_VALUE;
    }
}
