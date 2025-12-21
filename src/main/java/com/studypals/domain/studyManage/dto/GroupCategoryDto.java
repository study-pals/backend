package com.studypals.domain.studyManage.dto;

import com.studypals.domain.groupManage.service.GroupService;

/**
 * {@link GroupService} 의 getGroups() 메서드에서 [groupId : 그룹이 가지는 카테고리 id 리스트] Map을 만들기 위해 사용합니다.
 *
 * @author sleepyhoon
 * @see GroupService
 * @since 2025-12-21
 */
public record GroupCategoryDto(Long groupId, Long categoryId) {}
