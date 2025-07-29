package com.studypals.domain.studyManage.worker;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.studyManage.dao.StudyTimeRepository;
import com.studypals.global.annotations.Worker;

/**
 * <br>package name   : com.studypals.domain.studyManage.worker
 * <br>file name      : StudyTimeWriter
 * <br>date           : 7/29/25
 * <pre>
 * <span style="color: white;">[description]</span>
 *
 * </pre>
 * <pre>
 * <span style="color: white;">usage:</span>
 * {@code
 *
 * } </pre>
 */
@Worker
@RequiredArgsConstructor
public class StudyTimeWriter {

    private final StudyTimeRepository studyTimeRepository;

    public void changeStudyTimeToRemoved(Long userId, Long categoryId) {
        studyTimeRepository.markStudyTimeAsRemoved(userId, categoryId);
    }
}
