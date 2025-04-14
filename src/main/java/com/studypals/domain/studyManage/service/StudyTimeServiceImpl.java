package com.studypals.domain.studyManage.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.memberManage.dao.MemberRepository;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.dao.StudyCategoryRepository;
import com.studypals.domain.studyManage.dao.StudyStatusRedisRepository;
import com.studypals.domain.studyManage.dao.StudyTimeRepository;
import com.studypals.domain.studyManage.dto.StartStudyDto;
import com.studypals.domain.studyManage.dto.StartStudyReq;
import com.studypals.domain.studyManage.dto.mappers.StudyTimeMapper;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyStatus;
import com.studypals.domain.studyManage.entity.StudyTime;
import com.studypals.global.exceptions.errorCode.AuthErrorCode;
import com.studypals.global.exceptions.errorCode.StudyErrorCode;
import com.studypals.global.exceptions.exception.AuthException;
import com.studypals.global.exceptions.exception.StudyException;
import com.studypals.global.utils.TimeUtils;

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
    private final MemberRepository memberRepository;
    private final TimeUtils timeUtils;

    @Override
    public StartStudyDto startStudy(Long userId, StartStudyReq dto) {

        // 입력 검증
        validStartDto(dto);

        StudyStatus status = studyStatusRepository.findById(userId).orElse(null);

        if (status == null) {
            status = StudyStatus.builder()
                    .id(userId)
                    .startTime(dto.startAt())
                    .categoryId(dto.categoryId())
                    .temporaryName(dto.temporaryName())
                    .build();

            saveStatus(status);
        } else if (!status.isStudying()) {
            status = status.update()
                    .studying(true)
                    .startTime(dto.startAt())
                    .categoryId(dto.categoryId())
                    .temporaryName(dto.temporaryName())
                    .build();

            saveStatus(status);
        }

        return mapper.toDto(status);
    }

    @Override
    public Long endStudy(Long userId, LocalTime endedAt) {
        LocalDate today = timeUtils.getToday();
        StudyStatus status = studyStatusRepository.findById(userId).orElse(null);

        validStatus(status); // 받아온 status 가 정상인지 확인

        // 1) 실제 공부 시간(초)
        Long durationInSec = getTimeDuration(status.getStartTime(), endedAt);

        // 2) DB에 공부시간 upsert
        upsertStudyTime(userId, status, today, durationInSec);

        // 3) 레디스 상태 값 리셋
        resetStudyStatus(status, durationInSec);

        return durationInSec;
    }

    private void validStartDto(StartStudyReq dto) {
        if (dto.categoryId() != null && dto.temporaryName() != null) {
            throw new StudyException(StudyErrorCode.STUDY_TIME_START_FAIL, "both category, name exist");
        }

        if (dto.categoryId() == null && dto.temporaryName() == null) {
            throw new StudyException(StudyErrorCode.STUDY_TIME_START_FAIL, "both category, name null");
        }
    }

    private void validStatus(StudyStatus status) {

        if (status == null) {
            throw new StudyException(StudyErrorCode.STUDY_TIME_END_FAIL);
        } else if (status.getStartTime() == null || !status.isStudying()) {
            status = status.update()
                    .studying(false)
                    .startTime(null)
                    .categoryId(null)
                    .temporaryName(null)
                    .build();

            saveStatus(status);

            throw new StudyException(StudyErrorCode.STUDY_TIME_END_FAIL);
        }
    }

    private Long getTimeDuration(LocalTime start, LocalTime end) {

        long time;

        if (end.isBefore(start)) { // startAt 이 00:00 전이고, endedAt 이 이후인 경우
            long startToMidnight = Duration.between(start, LocalTime.MIDNIGHT).toSeconds();
            long midnightToEnd = Duration.between(LocalTime.MIN, end).toSeconds();
            time = startToMidnight + midnightToEnd;
        } else { // 정상적인 경우
            time = Duration.between(start, end).toSeconds();
        }
        return time;
    }

    private void upsertStudyTime(Long userId, StudyStatus status, LocalDate studiedAt, Long time) {

        Long categoryId = status.getCategoryId();
        String temporaryName = status.getTemporaryName();
        Member member = findMember(userId);

        member.addToken(calculateToken(time));

        if (categoryId != null) {
            Optional<StudyTime> optional =
                    studyTimeRepository.findByMemberIdAndStudiedAtAndCategoryId(userId, studiedAt, categoryId);
            if (optional.isPresent()) {
                optional.get().addTime(time);
            } else {
                createNewStudyTimeWithCategory(member, categoryId, studiedAt, time);
            }
        } else if (temporaryName != null) {
            Optional<StudyTime> optional =
                    studyTimeRepository.findByMemberIdAndStudiedAtAndTemporaryName(userId, studiedAt, temporaryName);
            if (optional.isPresent()) {
                optional.get().addTime(time);
            } else {
                createNewStudyTimeWithTemporaryName(member, temporaryName, studiedAt, time);
            }
        } else {
            throw new StudyException(StudyErrorCode.STUDY_TIME_END_FAIL, "both id, name null in redis");
        }
    }

    private void createNewStudyTimeWithCategory(Member member, Long categoryId, LocalDate studiedAt, Long time) {
        StudyCategory studyCategory = studyCategoryRepository.getReferenceById(categoryId);
        StudyTime newStudyTime = StudyTime.builder()
                .member(member)
                .category(studyCategory)
                .studiedAt(studiedAt)
                .time(time)
                .build();

        saveTime(newStudyTime);
    }

    private void createNewStudyTimeWithTemporaryName(
            Member member, String temporaryName, LocalDate studiedAt, Long time) {

        StudyTime newStudyTime = StudyTime.builder()
                .member(member)
                .temporaryName(temporaryName)
                .studiedAt(studiedAt)
                .time(time)
                .build();

        saveTime(newStudyTime);
    }

    private void resetStudyStatus(StudyStatus status, Long studiedTimeToAdd) {
        status = status.update()
                .studyTime(status.getStudyTime() + studiedTimeToAdd)
                .studying(false)
                .startTime(null)
                .categoryId(null)
                .temporaryName(null)
                .build();

        saveStatus(status);
    }

    private Member findMember(Long userId) {
        return memberRepository.findById(userId).orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));
    }

    private void saveStatus(StudyStatus status) {
        try {
            studyStatusRepository.save(status);
        } catch (Exception e) {
            throw new StudyException(StudyErrorCode.STUDY_TIME_END_FAIL, "save fail");
        }
    }

    private void saveTime(StudyTime time) {
        try {
            studyTimeRepository.save(time);
        } catch (Exception e) {
            throw new StudyException(StudyErrorCode.STUDY_TIME_END_FAIL, "save fail");
        }
    }

    private Long calculateToken(Long time) {
        return time / 60; // 어떤 값이 올지 몰라서 일단 1분당 토큰 1개 / 나중에 확정되면 숫자를 따로 뺄 예정
    }
}
