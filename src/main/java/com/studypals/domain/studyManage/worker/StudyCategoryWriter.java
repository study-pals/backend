package com.studypals.domain.studyManage.worker;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.studyManage.dao.PersonalStudyCategoryRepository;
import com.studypals.domain.studyManage.entity.PersonalStudyCategory;
import com.studypals.global.annotations.Worker;
import com.studypals.global.exceptions.errorCode.StudyErrorCode;
import com.studypals.global.exceptions.exception.StudyException;

/**
 * 공부 category 의 쓰기에 대한 로직을 수행합니다.
 *
 * <p><b>빈 관리:</b><br>
 * worker
 *
 * @author jack8
 * @since 2025-04-15
 */
@Worker
@RequiredArgsConstructor
public class StudyCategoryWriter {

    private final PersonalStudyCategoryRepository personalStudyCategoryRepository;

    /**
     * 카테고리를 저장하고, 안되면 적절한 예외를 생성합니다.
     * @param category 저장하고자 하는 카테고리
     */
    public void save(PersonalStudyCategory category) {
        try {
            personalStudyCategoryRepository.save(category);
        } catch (Exception e) {
            throw new StudyException(StudyErrorCode.STUDY_CATEGORY_ADD_FAIL);
        }
    }

    /**
     * 특정 카테고리를 삭제
     * @param category 삭제하고자 할, id 가 포함된 영속성 엔티티
     */
    public void delete(PersonalStudyCategory category) {

        personalStudyCategoryRepository.delete(category);
    }

    /**
     * 특정 유저의 카테고리를 전부 초기화
     * @param userId 삭제하고자 할 user의 아이디
     */
    public void deleteAll(Long userId) {

        personalStudyCategoryRepository.deleteByMemberId(userId);
    }
}
