package com.studypals.domain.studyManage.worker;

import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.studyManage.dao.StudyTimeRepository;
import com.studypals.domain.studyManage.dto.PeriodDto;
import com.studypals.domain.studyManage.entity.StudyTime;
import com.studypals.global.annotations.Worker;

/**
 * 공부 시간을 조회하는 데 사용하는 worker 클래스입니다.
 * StudyTime 엔티티에 대한 일반적인 생성(세션 제외) 및 통계를 위한 순차적 데이터를
 * 가져올 때 사용합니다.
 *
 * <p><b>빈 관리:</b><br>
 * Worker
 *
 * @author jack8
 * @since 2025-04-15
 */
@Worker
@RequiredArgsConstructor
public class StudyTimeReader {

    private final StudyTimeRepository studyTimeRepository;

    /**
     * 사용자 아이디와 날짜를 통해, 해당 날짜에서 해당 유저가 공부한 기록을 list 로 반환합니다.
     * @param userId 검색하고자 하는 user id
     * @param date 검색하고자 하는 날짜
     * @return StudyTime 에 대한 리스트
     */
    public List<StudyTime> findByUserIdAndDate(Long userId, LocalDate date) {
        return studyTimeRepository.findByMemberIdAndStudiedDate(userId, date);
    }

    /**
     * 사용자 아이디와, 특정 기간에 대해, 해당 유저가 공부한 기록에 대한 리스트를 반환합니다.
     * @param userId 검새갛조가 하는 user id
     * @param periodDto 기간에 대한 dto
     * @return StudyTime 에 대한 리스트
     */
    public List<StudyTime> findByUserIdAndPeriod(Long userId, PeriodDto periodDto) {
        return studyTimeRepository.findAllByMemberIdAndStudiedDateBetween(userId, periodDto.start(), periodDto.end());
    }
}
