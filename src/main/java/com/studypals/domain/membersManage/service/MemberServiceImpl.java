package com.studypals.domain.membersManage.service;

import com.studypals.domain.membersManage.dao.MemberRepository;
import com.studypals.domain.membersManage.dto.CreateMemberReq;
import com.studypals.domain.membersManage.entity.Member;
import com.studypals.global.exceptions.errorCode.AuthErrorCode;
import com.studypals.global.exceptions.exception.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * member service 의 구현 클래스입니다.
 * <p>
 * member 도메인에 대한 전반적인 로직을 수행합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link MemberService} 의 구현 클래스입니다.
 *
 * <p><b>빈 관리:</b><br>
 * Service
 *
 * @author jack8
 * @see MemberService
 * @since 2025-04-02
 */
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    @Transactional
    public Long createMember(CreateMemberReq dto) {
        Member member = Member.builder()
                .username(dto.username())
                .password(passwordEncoder.encode(dto.password()))
                .nickname(dto.nickname())
                .birthday(dto.birthday())
                .position(dto.position())
                .imageUrl(dto.imageUrl())
                .build();
        try {
            return memberRepository.save(member).getId();
        } catch (DataIntegrityViolationException e) {
            throw new AuthException(AuthErrorCode.SIGNUP_FAIL, "maybe duplicate username or nickname");
        }
    }
}
