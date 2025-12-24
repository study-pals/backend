package com.studypals.domain.groupManage.dto;

import com.studypals.domain.groupManage.entity.GroupRole;
import com.studypals.domain.groupManage.service.GroupService;

/**
 * {@link GroupService} 의 getGroups() 메서드에서 [groupId : 그룹에 속한 유저들 정보] Map을 만들기 위해 사용합니다.
 *
 * @author sleepyhoon
 * @see GroupService
 * @since 2025-12-21
 */
public record GroupMemberProfileMappingDto(
        Long groupId, String imageUrl, GroupRole role) {}
