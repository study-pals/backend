package com.studypals.domain.groupManage.fixture;

import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupMember;
import com.studypals.domain.memberManage.entity.Member;

public class GroupMemberFixture {

    public static GroupMember groupMember(Group group) {
        Member member = Member.builder().id(1L).build();
        return GroupMember.builder().id(1L).member(member).group(group).build();
    }
}
