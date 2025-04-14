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
 * {@link StudyTimeService} 의 구현 클래스입니다.
 * <p>
 * 오버라이드된 메서드와, 해당 메서드에 사용되는 private 메서드가 선언되어 있습니다. 자세한 주석은
 * interface 에 존재합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@code StudyTimeService} 의 구현 클래스
 *
 *
 * <p><b>빈 관리:</b><br>
 * Service 빈
 * <pre>
 *     private final StudyStatusRedisRepository studyStatusRepository;
 *     private final StudyTimeRepository studyTimeRepository;
 *     private final StudyCategoryRepository studyCategoryRepository;
 *     private final StudyTimeMapper mapper;
 *     private final MemberRepository memberRepository;
 *     private final TimeUtils timeUtils;
 * </pre>>
 *
 *
 * @author jack8
 * @see StudyTimeService
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

        // status 받아오기
        StudyStatus status = studyStatusRepository.findById(userId).orElse(null);

        // 오늘 처음 공부 시
        if (status == null) {
            status = StudyStatus.builder()
                    .id(userId)
                    .studying(true)
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

    /**
     * redis 에 저장된 정보가 유효한지 검증합니다. 검증 사항은 다음과 같습니다.
     * <pre>
     * 1. null 여부(사용자가 시작하지 않았음)
     * 2. studying == false 여부 (사용자가 시작하지 않았음)
     * 3. startTime null 여부(왜인지 모르겠으나 startTime이 들어가 있지 않음)
     * </pre>
     * null 이 아닌 경우, 잘못된 데이터가 존재하는 것이므로 공부 시간 누적을 제외하고 초기화 한다.
     * @param status redis에 저장된 사용자의 상태
     */
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

    /**
     * 시작 시간과 종료 시간에 대해 second로 반환하는 메서드.
     * 00:00 을 기점으로 계산 로직이 달라진다.
     * @param start 공부 시작 시간
     * @param end 공부 종료 시간
     * @return 초 단위 공부 시간
     */
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

    /**
     * studyTime 을 최신화하는 메서드. category에 대한 공부인지, temporaryName 에 대한 공부인지에 따라
     * 갈린다.
     * @param userId 사용자 id
     * @param status redis 에 저장된 사용자 상태
     * @param studiedAt 언제 공부했는지에 대한 날짜(today)
     * @param time 초 단위 공부 시간
     */
    private void upsertStudyTime(Long userId, StudyStatus status, LocalDate studiedAt, Long time) {

        Long categoryId = status.getCategoryId();
        String temporaryName = status.getTemporaryName();
        Member member = findMember(userId);

        // member 에 토큰을 업데이트
        member.addToken(calculateToken(time));

        if (categoryId != null) { // 카테고리에 대한 공부인 경우
            Optional<StudyTime> optional =
                    studyTimeRepository.findByMemberIdAndStudiedAtAndCategoryId(userId, studiedAt, categoryId);
            if (optional.isPresent()) {
                optional.get().addTime(time); // 이미 해당 레코드가 존재하면, 시간만 더해준다.
            } else {
                createNewStudyTimeWithCategory(member, categoryId, studiedAt, time); // 해당 레코드가 존재하지 않으면 새로 저장한다.
            }
        } else if (temporaryName != null) { // 임시 이름에 대한 공부인 경우
            Optional<StudyTime> optional =
                    studyTimeRepository.findByMemberIdAndStudiedAtAndTemporaryName(userId, studiedAt, temporaryName);
            if (optional.isPresent()) {
                optional.get().addTime(time); // 이미 존재하는 경우, 시간만 더해준다.
            } else {
                createNewStudyTimeWithTemporaryName(member, temporaryName, studiedAt, time); // 존재하지 않는 경우 새로 저장한다.
            }
        } else { // 모두 다 null 인 경우 실패
            throw new StudyException(StudyErrorCode.STUDY_TIME_END_FAIL, "both id, name null in redis");
        }
    }

    /**
     * 카테고리에 대한 공부 일 시, 해당 메서드를 통해 studyTime 테이블에 데이터를 저장한다.
     * @param member 사용자
     * @param categoryId 카테고리 아이디
     * @param studiedAt 공부 날짜(today)
     * @param time 초 단위 공부 시간
     */
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

    /**
     * 임시 이름에 대한 공부 일 시, 해당 메서드를 통해 studyTime 테이블에 데이터를 저장한다.
     * @param member 사용자
     * @param temporaryName 임시 이름
     * @param studiedAt 공부 날짜(today)
     * @param time 초 단위 공부 시간
     */
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

    /**
     * 공부 종료 후, 해당 메서드를 통해 사용자가 공부 중이 아님을 표시하낟.
     * @param status 기존에 존재하던 사용자의 공부 상태
     * @param studiedTimeToAdd 사용자가 추가로 공부한 시간
     */
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
