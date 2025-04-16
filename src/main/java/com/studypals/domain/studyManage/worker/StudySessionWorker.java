package com.studypals.domain.studyManage.worker;

import java.time.LocalDate;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.memberManage.dao.MemberRepository;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.dao.StudyCategoryRepository;
import com.studypals.domain.studyManage.dao.StudyTimeRepository;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyStatus;
import com.studypals.domain.studyManage.entity.StudyTime;
import com.studypals.global.annotations.Worker;
import com.studypals.global.exceptions.errorCode.AuthErrorCode;
import com.studypals.global.exceptions.errorCode.StudyErrorCode;
import com.studypals.global.exceptions.exception.AuthException;
import com.studypals.global.exceptions.exception.StudyException;

/**
 * service - repository 사이의 worker 계층입니다. 공부가 시작-종료 됨에 따라
 * 발생하는 로직들을 담당합니다.
 *
 * @author jack8
 * @since 2025-04-15
 */
@Worker
@RequiredArgsConstructor
public class StudySessionWorker {

    private final StudyTimeRepository studyTimeRepository;
    private final StudyCategoryRepository studyCategoryRepository;
    private final MemberRepository memberRepository;

    /**
     * studyTime 을 최신화하는 메서드. category에 대한 공부인지, temporaryName 에 대한 공부인지에 따라
     * 갈린다.
     * @param userId 사용자 id
     * @param status redis 에 저장된 사용자 상태
     * @param studiedAt 언제 공부했는지에 대한 날짜(today)
     * @param time 초 단위 공부 시간
     */
    public void upsertStudyTime(Long userId, StudyStatus status, LocalDate studiedAt, Long time) {

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
    public void createNewStudyTimeWithCategory(Member member, Long categoryId, LocalDate studiedAt, Long time) {
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
    public void createNewStudyTimeWithTemporaryName(
            Member member, String temporaryName, LocalDate studiedAt, Long time) {

        StudyTime newStudyTime = StudyTime.builder()
                .member(member)
                .temporaryName(temporaryName)
                .studiedAt(studiedAt)
                .time(time)
                .build();

        saveTime(newStudyTime);
    }

    private Member findMember(Long userId) {
        return memberRepository.findById(userId).orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));
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
