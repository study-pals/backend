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

    public Optional<StudyStatus> findAndDelete(Long id) {
        Optional<StudyStatus> result = studyStatusRedisRepository.findById(id);
        result.ifPresent(x -> studyStatusRedisRepository.deleteById(id));
        return result;
    }

    /**
     * 처음 공부 시작 시 객체를 생성합니다. 추가로, DailyStudyInfo를 생성하고 추가합니다.
     * @param dto 공부 데이터
     * @return 만들어진 객체
     */
    public StudyStatus startStatus(Member member, StartStudyReq dto) {

        if (!dailyStudyInfoRepository.existsByMemberIdAndStudiedDate(member.getId(), timeUtils.getToday())) {
            DailyStudyInfo summary = DailyStudyInfo.builder()
                    .member(member)
                    .studiedDate(timeUtils.getToday())
                    .startTime(dto.startTime())
                    .build();
            dailyStudyInfoRepository.save(summary);
        }

        return StudyStatus.builder()
                .id(member.getId())
                .categoryId(dto.categoryId())
                .studying(true)
                .startTime(dto.startTime())
                .name(dto.temporaryName())
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
                .categoryId(null)
                .name(null)
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
