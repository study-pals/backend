package com.studypals.domain.groupManage.dto.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupEntryRequest;
import com.studypals.domain.memberManage.entity.Member;

@Mapper(componentModel = "spring")
public interface GroupEntryRequestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    GroupEntryRequest toEntity(Member member, Group group);
}
