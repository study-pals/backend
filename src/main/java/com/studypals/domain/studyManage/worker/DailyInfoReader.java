package com.studypals.domain.studyManage.worker;

import java.util.List;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.studyManage.dao.DailyStudyInfoRepository;
import com.studypals.domain.studyManage.dto.PeriodDto;
import com.studypals.domain.studyManage.entity.DailyStudyInfo;
import com.studypals.global.annotations.Worker;

/**
 * 일/월 에 대한 시간 총합 데이터를 관리하는 reader 클래스입니다.
 *
 * @author jack8
 * @since 2025-04-17
 */
@Worker
@RequiredArgsConstructor
public class DailyInfoReader {

    private final DailyStudyInfoRepository dailyStudyInfoRepository;

    /**
     * 기간에 따라 해당 기간 동안의 공부 정보를 반환합니다. 시작 시간, 종료 시간, 공부 날짜 등에 대한 정보
     *
     * <p> NOT TESTED / simple logic </p>
     * @param userId 검색할 user id
     * @param period 검색할 기간
     * @return 공부 날짜, 시작 시간, 종료 시간, 메모의 엔티티
     */
    public List<DailyStudyInfo> getDailyInfoListByPeriod(Long userId, PeriodDto period) {
        return dailyStudyInfoRepository.findAllByMemberIdAndStudiedDateBetween(userId, period.start(), period.end());
    }
}
