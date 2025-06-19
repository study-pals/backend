package com.studypals.domain.memberManage.worker;

import java.util.List;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.memberManage.dao.MemberRepository;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.global.annotations.Worker;
import com.studypals.global.exceptions.errorCode.AuthErrorCode;
import com.studypals.global.exceptions.exception.AuthException;

/**
 * Member 에 대한 읽기 작업을 수행하는 worker 클래스입니다.
 * <p>
 * 여러 방법에 대한 검색 매커니즘을 수행합니다.
 *
 * <p><b>빈 관리:</b><br>
 * Worker
 *
 * @author jack8
 * @since 2025-04-15
 */
@Worker
@RequiredArgsConstructor
public class MemberReader {

    private final MemberRepository memberRepository;

    public Member get(Long userId) {
        return memberRepository
                .findById(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND, "can't get user"));
    }

    public Member get(String username) {
        return memberRepository
                .findByUsername(username)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND, "can't get user"));
    }

    public Member getRef(Long userId) {
        return memberRepository.getReferenceById(userId);
    }

    public List<Member> get(List<Long> userIds) {
        return memberRepository.findAllById(userIds);
    }
}
