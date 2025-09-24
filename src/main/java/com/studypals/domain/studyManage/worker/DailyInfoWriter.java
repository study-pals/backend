package com.studypals.domain.studyManage.worker;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.dao.DailyStudyInfoRepository;
import com.studypals.domain.studyManage.entity.DailyStudyInfo;
import com.studypals.global.annotations.Worker;
import com.studypals.global.exceptions.errorCode.StudyErrorCode;
import com.studypals.global.exceptions.exception.StudyException;

/**
 * dailyInfo 엔티티를 삭제/갱신/추가 등을 할 때 사용합니다.
 * @author jack8
 * @see DailyStudyInfo
 * @since 2025-04-19
 */
@Worker
@RequiredArgsConstructor
public class DailyInfoWriter {

    private final DailyStudyInfoRepository dailyStudyInfoRepository;

    /**
     * daily info 엔티티에서 종료 시간을 최신화 합니다. 종료 시간은 가장 마지막에 종료되는 시각이므로, 최신화가 되어야 합니다.
     * 종료 시간이 들어가지 않아있는 경우, 아침 6시로 간주하여야 합니다.
     * <p> NOT TESTED / simple logic </p>
     * @param member 갱신할 user 의 엔티티
     * @param studiedDate 공부 날짜(검색을 위한)
     * @param endTime 종료 시간(갱신을 위한)
     */
    public void updateEndtime(Member member, LocalDate studiedDate, LocalTime endTime) {
        DailyStudyInfo summary = dailyStudyInfoRepository
                .findByMemberIdAndStudiedDate(member.getId(), studiedDate)
                .orElseThrow(() -> new StudyException(
                        StudyErrorCode.STUDY_TIME_END_FAIL, "can't update end time in" + "[DailyInfoWriter]"));

        summary.setEndTime(endTime);
        dailyStudyInfoRepository.save(summary);
    }

    /**
     * 특정 날짜에 해당 인원이 생성한 dailyStudyInfo 엔티티가 존재하는지 여부를 검사합니다.
     * @param member 검사할 사용자 아이디
     * @param date 날짜
     * @return 존재하는지 여부에 대한 boolean
     */
    public boolean existsForMemberOnDate(Member member, LocalDate date) {
        return dailyStudyInfoRepository.existsByMemberIdAndStudiedDate(member.getId(), date);
    }

    /**
     * 만약 어떤 사용자가 특정 날짜에 DailyStudyInfo 를 생성한 적이 없다면, 이를 새롭게 생성하여 저장합니다.
     * endTime 을 함께 받아 시작 시각과 종료 시각이 특정된 경우 해당 메서드를 사용합니다.
     * @param member 사용자 아이디
     * @param studiedDate 공부 날짜
     * @param startTime 시작 시각
     * @param endTime 종료 시각
     * @return 만약 값이 존재하지 않아 새롭게 만들었으면 false 를 반환, 아니면 true 반환
     */
    public boolean createIfNotExist(Member member, LocalDate studiedDate, LocalTime startTime, LocalTime endTime) {
        if (!dailyStudyInfoRepository.existsByMemberIdAndStudiedDate(member.getId(), studiedDate)) {
            DailyStudyInfo dailyStudyInfo = DailyStudyInfo.builder()
                    .member(member)
                    .studiedDate(studiedDate)
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();
            dailyStudyInfoRepository.save(dailyStudyInfo);
            return true;
        }
        return false;
    }

    /**
     * 만약 어떤 사용자가 특정 날짜에 DailyStudyInfo 를 생성한 적이 없다면, 이를 새롭게 생성하여 저장합니다.
     * endTime 이 특정되지 않는 경우 해당 메서드를 사용합니다.
     * @param member 사용자 아이디
     * @param studiedDate 공부 날짜
     * @param startTime 시작 시각
     * @return 만약 값이 존재하지 않아 새롭게 만들었으면 false 를 반환, 아니면 true 반환
     */
    public boolean createIfNotExist(Member member, LocalDate studiedDate, LocalTime startTime) {
        return createIfNotExist(member, studiedDate, startTime, null);
    }
}
