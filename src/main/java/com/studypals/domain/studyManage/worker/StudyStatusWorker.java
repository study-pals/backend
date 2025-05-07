package com.studypals.domain.studyManage.worker;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.dao.DailyStudyInfoRepository;
import com.studypals.domain.studyManage.dao.StudyStatusRedisRepository;
import com.studypals.domain.studyManage.dto.StartStudyReq;
import com.studypals.domain.studyManage.entity.DailyStudyInfo;
import com.studypals.domain.studyManage.entity.StudyStatus;
import com.studypals.global.annotations.Worker;
import com.studypals.global.exceptions.errorCode.StudyErrorCode;
import com.studypals.global.exceptions.exception.StudyException;
import com.studypals.global.utils.TimeUtils;

/**
 * 공부 상태를 나타내는 studyStatus 의 저장/조회 및, 해당 객체의 생성에 대한
 * 역할을 수행합니다.
 *
 * <p><b>빈 관리:</b><br>
 * Worker
 *
 *
 * @author jack8
 * @see StudyStatus
 * @since 2025-04-15
 */
@Worker
@RequiredArgsConstructor
public class StudyStatusWorker {

    private final StudyStatusRedisRepository studyStatusRedisRepository;
    private final DailyStudyInfoRepository dailyStudyInfoRepository;
    private final TimeUtils timeUtils;

    /**
     * id에 대하여 studyStatus 를 redis로 부터 검색합니다.
     * @param id 검색하고자 하는 studyStatus의 id 이자, user의 id
     * @return Optional - study status
     */
    public Optional<StudyStatus> find(Long id) {
        return studyStatusRedisRepository.findById(id);
    }

    /**
     * 처음 공부 시작 시 객체를 생성합니다. 추가로, DailyStudyInfo를 생성하고 추가합니다.
     * @param dto 공부 데이터
     * @return 만들어진 객체
     */
    public StudyStatus firstStatus(Member member, StartStudyReq dto) {

        DailyStudyInfo summary = DailyStudyInfo.builder()
                .member(member)
                .studiedDate(timeUtils.getToday())
                .startTime(dto.startTime())
                .build();

        try {
            dailyStudyInfoRepository.save(summary);
        } catch (Exception e) {
            throw new StudyException(StudyErrorCode.STUDY_TIME_START_FAIL, "fail to create daily study info");
        }

        return StudyStatus.builder()
                .id(member.getId())
                .studying(true)
                .startTime(dto.startTime())
                .studyType(dto.type())
                .typeId(dto.typeId())
                .temporaryName(dto.temporaryName())
                .build();
    }

    /**
     * 공부 종료 후, 해당 메서드를 통해 사용자가 공부 중이 아님을 표시한다.
     * @param status 기존에 존재하던 사용자의 공부 상태
     * @param studiedTimeToAdd 사용자가 추가로 공부한 시간
     */
    public StudyStatus resetStatus(StudyStatus status, Long studiedTimeToAdd) {
        return status.update()
                .studyTime(status.getStudyTime() + studiedTimeToAdd)
                .studying(false)
                .startTime(null)
                .studyType(null)
                .typeId(null)
                .temporaryName(null)
                .build();
    }

    /**
     * 기존에 공부한 흔적이 redis에 있는 경우, 이를 업데이트하는 status 생성
     * @param status 기존에 존재하던 사용자의 공부 상태
     * @param dto 재시작하는 공부 정보
     */
    public StudyStatus restartStatus(StudyStatus status, StartStudyReq dto) {
        return status.update()
                .studying(true)
                .startTime(dto.startTime())
                .studyType(dto.type())
                .typeId(dto.typeId())
                .temporaryName(dto.temporaryName())
                .build();
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
    public void validStatus(StudyStatus status) {

        if (status == null) {
            throw new StudyException(StudyErrorCode.STUDY_TIME_END_FAIL);
        } else if (status.getStartTime() == null || !status.isStudying()) {

            resetAndSaveStatus(status);

            throw new StudyException(StudyErrorCode.STUDY_TIME_END_FAIL);
        }
    }

    private void resetAndSaveStatus(StudyStatus status) {
        StudyStatus updated = status.update()
                .studying(false)
                .startTime(null)
                .studyType(null)
                .typeId(null)
                .temporaryName(null)
                .build();

        saveStatus(updated);
    }

    public void saveStatus(StudyStatus status) {
        try {
            studyStatusRedisRepository.save(status);
        } catch (Exception e) {
            throw new StudyException(StudyErrorCode.STUDY_TIME_END_FAIL, "save fail");
        }
    }
}
