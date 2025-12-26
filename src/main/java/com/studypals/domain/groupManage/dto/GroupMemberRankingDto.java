package com.studypals.domain.groupManage.dto;

import com.studypals.domain.groupManage.entity.GroupRole;

public record GroupMemberRankingDto(Long id, String nickname, String imageUrl, Long studyTime, GroupRole role) {}
