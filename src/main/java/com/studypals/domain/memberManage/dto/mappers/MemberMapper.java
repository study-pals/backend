package com.studypals.domain.memberManage.dto.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.studypals.domain.memberManage.dto.CreateMemberReq;
import com.studypals.domain.memberManage.entity.Member;

/**
 * Member 에 대한 mapping 클래스입니다.
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
 * @since 2025-04-16
 */
@Mapper(componentModel = "spring")
public interface MemberMapper {

    @Mapping(target = "password", source = "encodedPassword") // password는 별도 인자
    @Mapping(target = "id", ignore = true) // DB가 자동 생성
    @Mapping(target = "createdDate", ignore = true) // Auditing에 의해 자동 생성
    @Mapping(target = "token", ignore = true) // 디폴트값으로 설정됨
    Member toEntity(CreateMemberReq req, String encodedPassword);
}
