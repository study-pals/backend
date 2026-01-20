package com.studypals.domain.groupManage.dto;

import java.util.List;

import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupMember;
import com.studypals.domain.memberManage.entity.Member;

public record GetGroupDetailRes(
        Long id,
        String name,
        String tag,
        List<String> hashTags,
        boolean isOpen,
        boolean isApprovalRequired,
        int totalMemberCount,
        int currentMemberCount,
        List<GroupMemberProfileDto> profiles,
        GroupTotalGoalDto groupGoals) {
    public static GetGroupDetailRes of(
            Group group, List<String> hashTags, List<GroupMember> groupMembers, GroupTotalGoalDto goals) {
        return new GetGroupDetailRes(
                group.getId(),
                group.getName(),
                group.getTag(),
                hashTags,
                group.isOpen(),
                group.isApprovalRequired(),
                group.getMaxMember(),
                group.getTotalMember(),
                groupMembers.stream()
                        .map(gm -> {
                            Member member = gm.getMember();
                            return new GroupMemberProfileDto(
                                    member.getId(), member.getNickname(), member.getImageUrl(), gm.getRole());
                        })
                        .toList(),
                goals);
    }
}
