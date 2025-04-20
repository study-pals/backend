package com.studypals.domain.studyManage.service;

import java.util.List;

import com.studypals.domain.studyManage.dto.GetDailyStudyInfoDto;
import com.studypals.domain.studyManage.dto.PeriodDto;

/**
 * DailyStudyInfo 에 대한 서비스 인터페이스입니다.
 * 주석 및 메서드 정의가 포함되어 있습니다.
 * dailyStudy 에 대한 읽기 등의 책임이 존재합니다.
 *
 * @author jack8
 * @see DailyStudyInfoServiceImpl
 * @since 2025-04-19
 */
public interface DailyStudyInfoService {
    /**
     * 유저와 기간을 받아서, 해당 기간에 대한 공부 정보를 반환합니다.
     * period 는 시작 날짜와 종료 날짜에 대한 dto 이며, 해당 날짜를 포함한 결과를 반환합니다.
     * 공부 날짜에 대해, 시작 시간, 종료 시간, 메모, 각 카테고리(임시 토픽) 별 공부 시간이 포함됩니다.
     * @param userId 검색하고자 하는 유저의 아이디
     * @param period 기간
     * @return 날짜 별, 시작, 종료 시간 및 카테고리 별 공부 시간
     */
    List<GetDailyStudyInfoDto> getDailyStudyInfoList(Long userId, PeriodDto period);
}
