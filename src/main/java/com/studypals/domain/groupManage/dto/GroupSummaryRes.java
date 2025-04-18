package com.studypals.domain.groupManage.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record GroupSummaryRes(
        Long id, String name, boolean isOpen, int totalMember, List<GroupMemberProfileDto> members) {}
