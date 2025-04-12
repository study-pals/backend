package com.studypals.domain.studyManage.service;

import java.util.List;

import com.studypals.domain.studyManage.dto.CreateCategoryReq;
import com.studypals.domain.studyManage.dto.DeleteCategoryReq;
import com.studypals.domain.studyManage.dto.UpdateCategoryReq;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.global.exceptions.exception.AuthException;
import com.studypals.global.exceptions.exception.StudyException;

/**
 * StudyCategory 에 대한 인터페이스.
 * 일부 메서드 주석이 포함된다.
 *
 * @author jack8
 * @see StudyCategoryServiceImpl
 * @since 2025-04-11
 */
public interface StudyCategoryService {

    /**
     * 카테고리 생성을 위한 메서드.
     * @param dto UserId 및 카테고리 정보가 포함
     * @return 생성된 category 의 id를 반환
     * @throws AuthException {@code AuthErrorCode.USER_NOT_FOUND, "In StudyCategoryServiceImpl} 포함
     */
    Long createCategory(CreateCategoryReq dto);

    /**
     * userId 를 통하여, 해당 유저가 보유한 StudyCategory 리스트를 반환하는 메서드.
     * @param userId 검색하고자 하는 유저의 id
     * @return 카테고리 리스트. 만약 없으면 빈 리스트가 반환된다.
     */
    List<StudyCategory> getUserCategory(Long userId);

    /**
     * 카테고리 update를 위한 메서드.
     * 요창자가 해당 카테고리의 소유주인지 확인하고, 갱신한다.
     * @param dto userId, categoryId 및 category 의 정보
     * @throws StudyException {@code StudyErrorCode.STUDY_CATEGORY_NOT_FOUND, "In StudyCategoryServiceImpl} 포함
     */
    void updateCategory(UpdateCategoryReq dto);

    /**
     * 카테고리 delete를 위한 메서드.
     * 요청자가 해당 카테고리의 소유주인지 확인하고, 삭제한다.
     * 여러 카테고리를 동시에 삭제할 수 있다.
     * @param dto userId 와 {@code List<CategoryId>}
     * @throws StudyException {@code StudyErrorCode.STUDY_CATEGORY_NOT_FOUND, "In StudyCategoryServiceImpl} 포함
     */
    void deleteCategories(DeleteCategoryReq dto);
}
