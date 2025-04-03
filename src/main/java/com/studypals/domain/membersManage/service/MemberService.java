package com.studypals.domain.membersManage.service;

import com.studypals.domain.membersManage.dto.CreateMemberReq;

/**
 * MemberService 의 인터페이스입니다. 메서드를 정의합니다.
 * <p>
 *
 * <p><b>상속 정보:</b><br>
 * MeberServiceImpl의 부모 인터페이스입니다.
 *
 * @author jack8
 * @see MemberServiceImpl
 * @since 2025-04-02
 */
public interface MemberService {
    Long createMember(CreateMemberReq dto);
}
