package com.studypals.domain.memberManage.dto.mappers;

import org.mapstruct.Mapper;

import com.studypals.domain.memberManage.dto.MemberDetailsRes;
import com.studypals.domain.memberManage.entity.Member;

/**
 * Member 에 대한 mapping 클래스입니다.
 * <p>
 * MapStruct을 통하여 자동으로 구현체가 생성됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * MapperStruct
 *
 * @author jack8
 * @since 2025-04-16
 */
@Mapper(componentModel = "spring")
public interface MemberMapper {

    MemberDetailsRes toRes(Member member);
}
