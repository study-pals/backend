package com.studypals.domain.memberManage.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.memberManage.dto.CheckDuplicateDto;
import com.studypals.domain.memberManage.dto.CreateMemberReq;
import com.studypals.domain.memberManage.dto.MemberDetailsRes;
import com.studypals.domain.memberManage.dto.UpdateProfileReq;
import com.studypals.domain.memberManage.dto.mappers.MemberMapper;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.worker.MemberReader;
import com.studypals.domain.memberManage.worker.MemberWriter;
import com.studypals.global.exceptions.errorCode.AuthErrorCode;
import com.studypals.global.exceptions.exception.AuthException;
import com.studypals.global.file.ObjectStorage;

/**
 * member service 의 구현 클래스입니다.
 *
 * <p>member 도메인에 대한 전반적인 로직을 수행합니다.
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

    private final MemberReader memberReader;
    private final MemberWriter memberWriter;
    private final PasswordEncoder passwordEncoder;
    private final MemberMapper memberMapper;

    private final ObjectStorage objectStorage;

    @Override
    @Transactional
    public Long createMember(CreateMemberReq dto) {
        String encoded = passwordEncoder.encode(dto.password());
        Member member = new Member(dto.username(), encoded, dto.nickname());

        memberWriter.save(member);

        return member.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getMemberIdByUsername(String username) {

        return memberReader.get(username).getId();
    }

    @Override
    @Transactional
    public Long updateProfile(Long userId, UpdateProfileReq dto) {
        Member member = memberReader.get(userId);

        member.updateProfile(dto.birthday(), dto.position());

        memberWriter.save(member);

        return member.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public MemberDetailsRes getProfile(Long userId) {
        Member member = memberReader.get(userId);
        return memberMapper.toRes(member, objectStorage);
    }

    @Override
    public boolean duplicateCheck(CheckDuplicateDto dto) {

        boolean hasUsername = dto.username() != null && !dto.username().isBlank();
        boolean hasNickname = dto.nickname() != null && !dto.nickname().isBlank();

        if (hasUsername == hasNickname && hasUsername) {
            throw new AuthException(
                    AuthErrorCode.SIGNUP_FAIL,
                    "username 혹은 nickname 중 하나만 존재해야 합니다.",
                    "[MemberController#checkAvailability] username & nickname both exists");
        }

        if (hasUsername == hasNickname) {
            throw new AuthException(
                    AuthErrorCode.SIGNUP_FAIL,
                    "username 혹은 nickname 중 하나는 필수입니다.",
                    "[MemberController#checkAvailability] username & nickname both blank");
        }

        return hasUsername
                ? memberReader.existsByUsername(dto.username())
                : memberReader.existsByNickname(dto.nickname());
    }
}
