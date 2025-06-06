package com.studypals.domain.studyManage.dto.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.dto.CreateCategoryReq;
import com.studypals.domain.studyManage.dto.GetCategoryRes;
import com.studypals.domain.studyManage.entity.StudyCategory;

/**
 * StudyCategory 에 대한 mapping 클래스입니다.
 * <p>
 * MapStruct을 통하여 자동으로 구현체가 생성됩니다.
 *
 * <p><b>빈 관리:</b><br>
 * Mapper 어노테이션을 통해 자동으로 빈에 {@code CategoryMapper}로 등록됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * MapperStruct
 *
 * @author jack8
 * @since 2025-04-12
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "member", source = "member")
    StudyCategory toEntity(CreateCategoryReq req, Member member);

    @Mapping(source = "id", target = "typeId")
    @Mapping(target = "studyType", constant = "PERSONAL")
    GetCategoryRes toDto(StudyCategory entity);
}
