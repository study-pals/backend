package com.studypals.domain.groupManage.dto.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.studypals.domain.groupManage.dto.CreateGroupReq;
import com.studypals.domain.groupManage.dto.GetGroupTagRes;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupTag;

@Mapper(componentModel = "spring")
public interface GroupMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "totalMember", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    Group toEntity(CreateGroupReq dto);

    GetGroupTagRes toTagDto(GroupTag entity);
}
