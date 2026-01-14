package com.studypals.domain.groupManage.dto.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.studypals.domain.groupManage.dto.CreateGroupReq;
import com.studypals.domain.groupManage.dto.GetGroupTagRes;
import com.studypals.domain.groupManage.dto.GroupSummaryDto;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupTag;

@Mapper(componentModel = "spring")
public interface GroupMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "totalMember", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "chatRoom", ignore = true)
    Group toEntity(CreateGroupReq dto);

    GetGroupTagRes toTagDto(GroupTag entity);

    @Mapping(target = "memberCount", source = "totalMember")
    @Mapping(target = "chatRoomId", source = "chatRoom.id")
    @Mapping(target = "open", source = "open")
    @Mapping(target = "approvalRequired", source = "approvalRequired")
    GroupSummaryDto toDto(Group group);
}
