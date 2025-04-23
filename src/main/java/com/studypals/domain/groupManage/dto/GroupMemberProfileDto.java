package com.studypals.domain.groupManage.dto;

import com.studypals.domain.groupManage.entity.GroupRole;

public record GroupMemberProfileDto(Long id, String nickname, String imageUrl, GroupRole role) {}
