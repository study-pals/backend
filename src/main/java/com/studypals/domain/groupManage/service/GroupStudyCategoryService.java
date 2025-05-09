package com.studypals.domain.groupManage.service;

import java.util.List;

import com.studypals.domain.studyManage.dto.GetCategoryRes;

/**
 * GroupStudyCategoryService 의 인터페이스입니다. 메서드를 정의합니다.
 *
 * <p>
 *
 * <p><b>상속 정보:</b><br>
 * GroupStudyCategoryServiceImpl의 부모 인터페이스입니다.
 *
 * @author s0o0bn
 * @see GroupStudyCategoryServiceImpl
 * @since 2025-05-08
 */
public interface GroupStudyCategoryService {

    /**
     * 그룹의 전체 카테고리 목록을 조회한다.
     *
     * @param groupId 조회할 그룹 ID
     * @return 그룹의 카테고리 리스트
     * @see GetCategoryRes
     */
    List<GetCategoryRes> getGroupCategory(Long groupId);
}
