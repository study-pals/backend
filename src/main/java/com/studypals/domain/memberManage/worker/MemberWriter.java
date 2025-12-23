package com.studypals.domain.memberManage.worker;

import org.springframework.dao.DataIntegrityViolationException;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.memberManage.dao.MemberRepository;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.global.annotations.Worker;
import com.studypals.global.exceptions.errorCode.AuthErrorCode;
import com.studypals.global.exceptions.exception.AuthException;

/**
 * Member 에 대한 쓰기 작업을 수행하는 worker 클래스입니다.
 * <p>
 * 저장, 갱신, 삭제 등의 매커니즘을 수행합니다.
 *
 * <p><b>빈 관리:</b><br>
 * Worker
 *
 * @author jack8
 * @since 2025-04-16
 */
@Worker
@RequiredArgsConstructor
public class MemberWriter {

    private final MemberRepository memberRepository;

    public void save(Member member) {

        try {
            memberRepository.save(member);
        } catch (DataIntegrityViolationException e) { // client message 수정
            throw new AuthException(
                    AuthErrorCode.SIGNUP_FAIL,
                    "[MemberWriter#save] duplicate | " + getDuplicateLogMessage(member),
                    "username 혹은 nickname 이 중복되었습니다.");
        }
    }

    private String getDuplicateLogMessage(Member member) {
        return "username : " + member.getUsername() + " | nickname : " + member.getNickname();
    }
}
