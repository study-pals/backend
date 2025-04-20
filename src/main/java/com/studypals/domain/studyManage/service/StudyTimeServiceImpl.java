package com.studypals.domain.studyManage.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.studyManage.dto.GetDailyStudyDto;
import com.studypals.domain.studyManage.dto.GetStudyDto;
import com.studypals.domain.studyManage.dto.PeriodDto;
import com.studypals.domain.studyManage.dto.StudyList;
import com.studypals.domain.studyManage.dto.mappers.StudyTimeMapper;
import com.studypals.domain.studyManage.entity.StudyTime;
import com.studypals.domain.studyManage.worker.StudyTimeReader;
import com.studypals.global.utils.TimeUtils;

/**
 * {@link StudyTimeService} 의 구현 클래스입니다.
 * <p>
 * 오버라이드된 메서드가 존재합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@code StudyTimeService} 의 구현 클래스
 *
 * <p><b>빈 관리:</b><br>
 * Service 빈
 * <pre>
 * private final TimeUtils timeUtils;
 * private final StudyTimeMapper studyTimeMapper;
 *
 * private final StudyTimeReader studyTimeReader;
 * </pre>>
 *
 *
 * @author jack8
 * @see StudyTimeService
 * @since 2025-04-14
 */
@Service
@RequiredArgsConstructor
public class StudyTimeServiceImpl implements StudyTimeService {

    private final TimeUtils timeUtils;
    private final StudyTimeMapper studyTimeMapper;

    private final StudyTimeReader studyTimeReader;

    @Override
    @Transactional(readOnly = true)
    public List<GetStudyDto> getStudyList(Long userId, LocalDate date) {
        LocalDate today = timeUtils.getToday();
        if (date.isAfter(today)) {
            return List.of();
        }

        List<StudyTime> times = studyTimeReader.getListByMemberAndDate(userId, date);

        return times.stream().map(studyTimeMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetDailyStudyDto> getDailyStudyList(Long userId, PeriodDto period) {
        List<StudyTime> summaries = studyTimeReader.getListByMemberAndDateByPeriod(userId, period);

        return summaries.stream()
                .collect(Collectors.groupingBy(StudyTime::getStudiedAt))
                .entrySet()
                .stream() // entry 로 변환
                .map(entry -> new GetDailyStudyDto(
                        entry.getKey(), // entry 의 키는 그룹화 한 기준
                        entry.getValue().stream() // 값은 해당 객체
                                .map(st -> new StudyList(
                                        st.getCategory() != null // 카테고리가 null 이면 temporaryName 을 써야 하므로
                                                ? st.getCategory().getId()
                                                : null,
                                        st.getTemporaryName(),
                                        st.getTime()))
                                .toList()))
                .sorted(Comparator.comparing(GetDailyStudyDto::studiedAt)) // 공부 날짜에 따라 정렬
                .toList();
    }
}
