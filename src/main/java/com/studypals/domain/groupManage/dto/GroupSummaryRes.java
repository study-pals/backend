package com.studypals.domain.groupManage.dto;

import java.util.List;

import lombok.Builder;

import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupRole;

/**
 * 클라이언트가 초대 코드로 그룹 요약 정보를 요청하면, 아래 정보를 반환합니다.
 * 그룹 ID, 그룹명, 태그, 공개여부, 총 그룹원 수, 대표 그룹원 프로필 리스트
 *
 * @author s0o0bn
 * @since 2025-04-19
 */
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
