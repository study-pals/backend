package com.studypals.domain.studyManage.service;

import java.time.LocalDate;
import java.util.List;

import com.studypals.domain.studyManage.dto.GetDailyStudyDto;
import com.studypals.domain.studyManage.dto.GetStudyDto;
import com.studypals.domain.studyManage.dto.PeriodDto;

/**
 * 공부 시간을 반환하거나, 통계를 담당합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link StudyTimeServiceImpl} 에 대한 인터페이스입니다.
 *
 * @author jack8
 * @see StudyTimeServiceImpl
 * @since 2025-04-14
 */
public interface StudyTimeService {
    /**
     * studies 를 반환합니다. 이는 StudyTime 테이블에 특정 날짜에 대한 유저의 카테고리-공부시간 에 대한
     * 리스트를 반환합니다. 영속화된 카테고리의 경우 categoryId 와 name 이 모두 존재합니다. 임시 카테고리의 경우 name 만 존재합니다.
     * 만약 "오늘"보다 이후의 날짜가 들어오면 빈 리스트를 반환합니다.
     * 해당 날짜에 아무런 데이터가 없으면 빈 리스트를 반환합니다.
     * @param userId 찾고자 하는 user의 id
     * @param date 찾고자 하는 날짜
     * @return 해당 날짜에 사용자가 공부한 카테고리id or 이름 - 시간(초 단위)
     */
    List<GetStudyDto> getStudyList(Long userId, LocalDate date);

    /**
     * 워커 클래스로부터 StudyTime 에 대한 리스트를 받아, 이를 GetDailyStudyDto 로 변환하여 반환합니다.
     * StudyTime 은 하나의 (날짜 - 카테고리(임시 토픽) - 공부 시간) 으로 이루어 있고 이를 날짜로 그룹화하여,
     * 각 날짜 별로 하나의 객체로 간추려 반환하도록 하였습니다.
     * @param userId 검색하고자 하는 user의 id
     * @param period 기간
     * @return 간추린 정보
     */
    List<GetDailyStudyDto> getDailyStudyList(Long userId, PeriodDto period);
}
