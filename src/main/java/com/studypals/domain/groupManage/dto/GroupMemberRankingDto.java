package com.studypals.domain.groupManage.dto;

import com.studypals.domain.groupManage.entity.GroupRole;
import java.time.LocalDate;

public record GroupMemberRankingDto(
        Long id, String nickname, String imageUrl, LocalDate studyTime, GroupRole role
) {}
