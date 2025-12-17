package com.studypals.domain.memberManage.service;

import com.studypals.domain.memberManage.dto.CreateMemberReq;
import com.studypals.domain.memberManage.dto.MemberDetailsRes;
import com.studypals.domain.memberManage.dto.UpdateProfileReq;

/**
 * MemberService 의 인터페이스입니다. 메서드를 정의합니다.
 *
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

    Long getMemberIdByUsername(String username);

    Long updateProfile(Long userId, UpdateProfileReq dto);

    MemberDetailsRes getProfile(Long userId);
}
