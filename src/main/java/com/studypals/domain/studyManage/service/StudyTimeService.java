package com.studypals.domain.studyManage.service;

import java.time.LocalDate;
import java.util.List;

import com.studypals.domain.studyManage.dto.GetStudyListDto;

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
     * studyList 를 반환합니다. 이는 StudyTime 테이블에 특정 날짜에 대한 유저의 카테고리-공부시간 에 대한
     * 리스트를 반환합니다. 카테고리 대신 temporaryName 이 들어가 있을 수도 있습니다.
     * 만약 "오늘"보다 이후의 날짜가 들어오면 빈 리스트를 반환합니다.
     * 해당 날짜에 아무런 데이터가 없으면 빈 리스트를 반환합니다.
     * @param userId 찾고자 하는 user의 id
     * @param date 찾고자 하는 날짜
     * @return 해당 날짜에 사용자가 공부한 카테고리id or 이름 - 시간(초 단위)
     */
    List<GetStudyListDto> getStudyList(Long userId, LocalDate date);
}
