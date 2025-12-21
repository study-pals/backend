package com.studypals.domain.groupManage.dto;

import com.studypals.domain.groupManage.entity.GroupRole;

public record GroupMemberProfileMappingDto(
        Long groupId, Long userId, String nickname, String imageUrl, GroupRole role) {}
