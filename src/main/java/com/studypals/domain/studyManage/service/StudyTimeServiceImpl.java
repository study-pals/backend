package com.studypals.domain.studyManage.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.studyManage.dao.StudyTimeRepository;
import com.studypals.domain.studyManage.dto.GetStudyDto;
import com.studypals.domain.studyManage.dto.mappers.StudyTimeMapper;
import com.studypals.domain.studyManage.entity.StudyTime;
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
 *     private final TimeUtils timeUtils;
 *     private final StudyTimeMapper mapper;
 *     private final StudyTimeRepository studyTimeRepository;
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
    private final StudyTimeMapper mapper;
    private final StudyTimeRepository studyTimeRepository;

    @Override
    public List<GetStudyDto> getStudyList(Long userId, LocalDate date) {
        LocalDate today = timeUtils.getToday();
        if (date.isAfter(today)) {
            return List.of();
        }

        List<StudyTime> times = studyTimeRepository.findByMemberIdAndStudiedAt(userId, date);

        return times.stream().map(mapper::toDto).toList();
    }
}
