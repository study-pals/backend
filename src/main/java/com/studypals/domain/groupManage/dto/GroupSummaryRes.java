package com.studypals.domain.groupManage.dto;

import java.util.List;

import lombok.Builder;

import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupRole;

@Builder
public record GroupSummaryRes(
        Long id, String name, String tag, boolean isOpen, int memberCount, List<GroupMemberProfileImageDto> profiles) {

    public record GroupMemberProfileImageDto(String imageUrl, GroupRole role) {}

    public static GroupSummaryRes of(Group group, List<GroupMemberProfileDto> profiles) {
        return GroupSummaryRes.builder()
                .id(group.getId())
                .name(group.getName())
                .tag(group.getTag())
                .isOpen(group.isOpen())
                .memberCount(group.getTotalMember())
                .profiles(profiles.stream()
                        .map(it -> new GroupMemberProfileImageDto(it.imageUrl(), it.role()))
                        .toList())
                .build();
    }
}
