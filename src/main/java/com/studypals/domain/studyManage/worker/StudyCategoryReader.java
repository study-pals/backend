package com.studypals.domain.studyManage.worker;

import java.util.List;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.studyManage.dao.PersonalStudyCategoryRepository;
import com.studypals.domain.studyManage.entity.PersonalStudyCategory;
import com.studypals.global.annotations.Worker;
import com.studypals.global.exceptions.errorCode.StudyErrorCode;
import com.studypals.global.exceptions.exception.StudyException;

/**
 * 공부 category 의 읽기에 대한 로직을 수행합니다.
 *
 * <p><b>빈 관리:</b><br>
 * worker
 *
 * @author jack8
 * @since 2025-04-17
 */
@Worker
@RequiredArgsConstructor
public class StudyCategoryReader {

    private final PersonalStudyCategoryRepository personalStudyCategoryRepository;

    /**
     * 특정 멤버의 모든 카테고리를 반환하는 메서드
     * @param userId 검색할 user 의 아이디
     * @return 카테고리 리스트
     */
    public List<PersonalStudyCategory> findByMember(Long userId) {
        return personalStudyCategoryRepository.findByMemberId(userId);
    }

    /**
     * 특정 날짜의, 해당 유저의 카테고리를 가져오는 메서드. dayBelong이 0인 경우 주간 카테고리이므로 해당 데이터도 가져온다.
     * @param userId 검색할 유저의 아이디
     * @param dayBit 검색할 요일 / 비트 / 가령 수요일이면 0b0000100 (4) 로 정의
     * @return 카테고리 리스트
     */
    public List<PersonalStudyCategory> getListByMemberAndDay(Long userId, int dayBit) {

        return personalStudyCategoryRepository.findByMemberId(userId).stream()
                .filter(category -> ((category.getDayBelong() & dayBit) != 0) || category.getDayBelong() == 0)
                .toList();
    }

    /**
     * 특정 카테고리를 category id 를 기반으로 찾되, 해당 카테고리의 소유주를 검증하고 반환
     * @param userId 검증할 유저
     * @param categoryId 검색할 유저
     * @return 만약 해당 카테고리가 해당 유저의 소유라면, 카테고리 반환
     */
    public PersonalStudyCategory getAndValidate(Long userId, Long categoryId) {
        PersonalStudyCategory category = personalStudyCategoryRepository
                .findById(categoryId)
                .orElseThrow(() ->
                        new StudyException(StudyErrorCode.STUDY_CATEGORY_NOT_FOUND, "In StudyCategoryServiceImpl"));

        if (!category.isOwner(userId)) {
            throw new StudyException(StudyErrorCode.STUDY_CATEGORY_DELETE_FAIL, "owner of category does not match");
        }

        return category;
    }
}
