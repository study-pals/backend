package com.studypals.domain.groupManage.fixture;

import com.studypals.domain.groupManage.dto.CreateGroupReq;
import com.studypals.domain.groupManage.entity.Group;

public class GroupFixture {

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
