package com.studypals.domain.studyManage.service;

import java.util.List;

import com.studypals.domain.studyManage.dto.CreateCategoryReq;
import com.studypals.domain.studyManage.dto.GetCategoryRes;
import com.studypals.domain.studyManage.dto.UpdateCategoryReq;
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
     * @param userId 생성하는 user의 id
     * @param dto 카테고리 정보가 포함
     * @return 생성된 category 의 id를 반환
     * @throws AuthException {@code AuthErrorCode.USER_NOT_FOUND, "In StudyCategoryServiceImpl} 포함
     */
    Long createCategory(Long userId, CreateCategoryReq dto);

    /**
     * userId 를 통하여, 해당 유저가 보유한 StudyCategory 리스트를 반환하는 메서드.
     * @param userId 검색하고자 하는 유저의 id
     * @return 카테고리 리스트. 만약 없으면 빈 리스트가 반환된다.
     */
    List<GetCategoryRes> getUserCategory(Long userId);

    /**
     * 카테고리 update를 위한 메서드.
     * 요창자가 해당 카테고리의 소유주인지 확인하고, 갱신한다.
     * @param userId 갱신을 요청한 사용자의 id
     * @param dto categoryId 및 category 의 정보
     * @throws StudyException {@code StudyErrorCode.STUDY_CATEGORY_NOT_FOUND, "In StudyCategoryServiceImpl} 포함
     */
    void updateCategory(Long userId, UpdateCategoryReq dto);

    /**
     * 카테고리 delete를 위한 메서드.
     * 요청자가 해당 카테고리의 소유주인지 확인하고, 삭제한다.
     * @param userId 삭제 요청자의 id
     * @param categoryId 삭제할 카테고리의 id
     * @throws StudyException {@code StudyErrorCode.STUDY_CATEGORY_NOT_FOUND, "In StudyCategoryServiceImpl} 포함
     */
    void deleteCategory(Long userId, Long categoryId);

    /**
     * 특정 요일의 카테고리를 전부 삭제한다.
     * @param userId 삭제를 요청한 요청자의 id
     */
    void initCategory(Long userId);
}
