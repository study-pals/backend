package com.studypals.domain.studyManage.service;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.studyManage.dao.StudyCategoryRepository;
import com.studypals.domain.studyManage.dao.StudyStatusRedisRepository;
import com.studypals.domain.studyManage.dao.StudyTimeRepository;
import com.studypals.domain.studyManage.dto.StartStudyDto;
import com.studypals.domain.studyManage.dto.StartStudyReq;
import com.studypals.domain.studyManage.dto.mappers.StudyTimeMapper;
import com.studypals.domain.studyManage.entity.StudyStatus;
import com.studypals.global.exceptions.errorCode.StudyErrorCode;
import com.studypals.global.exceptions.exception.StudyException;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ExampleClass(String example)}  <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author jack8
 * @see
 * @since 2025-04-13
 */
@Service
@RequiredArgsConstructor
public class StudyTimeServiceImpl implements StudyTimeService {

    private final StudyStatusRedisRepository studyStatusRepository;
    private final StudyTimeRepository studyTimeRepository;
    private final StudyCategoryRepository studyCategoryRepository;
    private final StudyTimeMapper mapper;

    @Override
    public StartStudyDto startStudy(Long userId, StartStudyReq dto) {
        StudyStatus status = studyStatusRepository.findById(userId).orElse(null);

        if (status == null) {
            status = StudyStatus.builder()
                    .id(userId)
                    .startTime(dto.startAt())
                    .categoryId(dto.categoryId())
                    .build();

            studyStatusRepository.save(status);
        } else if (!status.isStudying()) {
            status = status.update()
                    .studying(true)
                    .startTime(dto.startAt())
                    .categoryId(dto.categoryId())
                    .build();
            studyStatusRepository.save(status);
        }

        return mapper.toDto(status);
    }

    @Override
    public Long endStudy(Long userId, LocalDateTime endedAt) {
        StudyStatus status = studyStatusRepository.findById(userId).orElse(null);

        if (status == null) {
            throw new StudyException(StudyErrorCode.STUDY_TIME_END_FAIL);
        } else if (status.getStartTime() == null || !status.isStudying()) {
            status.update().studying(false).studyTime(null).categoryId(null).build();
            studyStatusRepository.save(status);

            throw new StudyException(StudyErrorCode.STUDY_TIME_END_FAIL);
        }

        Long studyTime = Duration.between(status.getStartTime(), endedAt).toSeconds();
        status.update()
                .studyTime(status.getStudyTime() + studyTime)
                .studying(false)
                .categoryId(null)
                .build();
        studyStatusRepository.save(status);

        return studyTime;
    }
}
