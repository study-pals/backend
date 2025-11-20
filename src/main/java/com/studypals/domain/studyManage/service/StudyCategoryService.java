package com.studypals.domain.studyManage.service;

import java.util.List;

import com.studypals.domain.studyManage.dto.CreateCategoryDto;
import com.studypals.domain.studyManage.dto.GetCategoryRes;
import com.studypals.domain.studyManage.dto.UpdateCategoryReq;
import com.studypals.global.exceptions.exception.AuthException;
import com.studypals.global.exceptions.exception.StudyException;

/**
 * PersonalStudyCategory 에 대한 인터페이스.
 * 일부 메서드 주석이 포함된다.
 *
 * @author jack8
 * @see StudyCategoryServiceImpl
 * @since 2025-04-11
 */
public interface StudyCategoryService {

    /**
     * 카테고리를 생성합니다.
     * @param dto 카테고리 정보가 포함
     * @return 생성된 category 의 id를 반환
     * @throws AuthException {@code AuthErrorCode.USER_NOT_FOUND, "In StudyCategoryServiceImpl} 포함
     */
    Long createCategory(Long userId, CreateCategoryDto dto);

    /**
     * 특정 유저가 표시할 수 있는 모든 카테고리 리스트 정보를 가져옵니다.
     * @param userId 검색하고자 하는 유저의 id
     * @return 카테고리 리스트. 만약 없으면 빈 리스트가 반환된다.
     */
    List<GetCategoryRes> getAllUserCategories(Long userId);

    /**
     * 특정 그룹이 정의한 카테고리 리스트 정보를 가져옵니다.
     * @param groupId 그룹 아이디 정보
     * @return 카테고리 리스트. 만약 없으면 빈 리스트가 반환된다.
     */
    List<GetCategoryRes> getGroupCategories(Long groupId);

    /**
     * 카테고리의 update 를 담당합니다. {@link UpdateCategoryReq} 의 필드가 null 인 경우 해당 값은 보통 갱신되지 않습니ㅏㄷ.
     * 자세한 정보는 {@link com.studypals.domain.studyManage.worker.StudyCategoryWriter} 에서 확인 가능합니다.
     * @param userId 갱신을 요청한 사용자의 id
     * @param dto categoryId 및 category 의 정보
     * @return cateogoryId 변경된 카테고리의 id
     * @throws StudyException {@code StudyErrorCode.STUDY_CATEGORY_NOT_FOUND, "In StudyCategoryServiceImpl} 포함
     */
    Long updateCategory(Long userId, UpdateCategoryReq dto);

    /**
     * 카테고리 delete를 위한 메서드.
     * 다만 실제로 삭제되는 것이 아닌, StudyType 이 REMOVED / GROUP_REMOVED로 변경됩니다.
     * @param userId 삭제 요청자의 id
     * @param categoryId 삭제할 카테고리의 id
     * @throws StudyException {@code StudyErrorCode.STUDY_CATEGORY_NOT_FOUND, "In StudyCategoryServiceImpl} 포함
     */
    void deleteCategory(Long userId, Long categoryId);
}
