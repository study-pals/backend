package com.studypals.domain.groupManage.dto.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupMainColor;
import com.studypals.domain.groupManage.entity.GroupMember;
import com.studypals.domain.groupManage.entity.GroupRole;
import com.studypals.domain.memberManage.entity.Member;

@Mapper(componentModel = "spring")
public interface GroupMemberMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "joinedAt", ignore = true)
    GroupMember toEntity(Member member, Group group);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "joinedAt", ignore = true)
    GroupMember toEntity(Member member, Group group, GroupRole role);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "joinedAt", ignore = true)
    GroupMember toEntity(Member member, Group group, GroupRole role, GroupMainColor mainColor);
}
