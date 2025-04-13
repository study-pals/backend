package com.studypals.domain.groupManage.fixture;

import java.util.List;

import com.studypals.domain.groupManage.dto.CreateGroupReq;
import com.studypals.domain.groupManage.dto.GetGroupTagRes;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupTag;

public class GroupFixture {

    public static List<GroupTag> groupTags() {
        return List.of(new GroupTag("tag1"), new GroupTag("tag2"), new GroupTag("tag3"));
    }

    public static List<GetGroupTagRes> getGroupTagRes(List<GroupTag> tags) {
        return tags.stream().map(tag -> new GetGroupTagRes(tag.getName())).toList();
    }

    public static Group group(CreateGroupReq req) {
        return Group.builder()
                .id(1L)
                .name(req.name())
                .tag(req.tag())
                .maxMember(req.maxMember())
                .isOpen(req.isOpen())
                .isApprovalRequired(req.isApprovalRequired())
                .build();
    }

    public static CreateGroupReq createGroupReq() {
        return new CreateGroupReq("group name", "group tag", 10, false, false);
    }
}
