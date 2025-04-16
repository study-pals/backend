package com.studypals.domain.studyManage.worker;

import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.studyManage.dao.StudyTimeRepository;
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
public class StudyTimeWorker {

    private final StudyTimeRepository studyTimeRepository;

    public List<StudyTime> findDateStudyByMember(Long userId, LocalDate date) {
        return studyTimeRepository.findByMemberIdAndStudiedAt(userId, date);
    }
}
