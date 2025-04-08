package com.studypals.domain.memberManage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.studypals.domain.memberManage.dao.MemberRepository;
import com.studypals.domain.memberManage.dto.CreateMemberReq;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.global.exceptions.errorCode.AuthErrorCode;
import com.studypals.global.exceptions.exception.AuthException;

/**
 * {@link MemberService} 에 대한 단위 테스트입니다.
 *
 * @author jack8
 * @see MemberService
 * @since 2025-04-07
 */
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock private MemberRepository memberRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private Member mockMember;
    @InjectMocks private MemberServiceImpl memberService;

    @Test
    void createMember_success() {
        // given
        CreateMemberReq dto =
                new CreateMemberReq(
                        "username",
                        "password",
                        "nickname",
                        LocalDate.of(1999, 8, 20),
                        "student",
                        "example.com");

        given(passwordEncoder.encode("password")).willReturn("encoded password");
        given(memberRepository.save(any())).willReturn(mockMember);
        given(mockMember.getId()).willReturn(1L);

        // when
        Long id = memberService.createMember(dto);

        // then
        assertThat(id).isEqualTo(1L);
        then(passwordEncoder).should().encode("password");
        then(memberRepository).should().save(any());
    }

    @Test
    void createMember_fail_dupliateUser() {
        // given
        CreateMemberReq dto =
                new CreateMemberReq(
                        "username",
                        "password",
                        "nickname",
                        LocalDate.of(1999, 8, 20),
                        "student",
                        "example.com");

        given(passwordEncoder.encode("password")).willReturn("encoded password");
        given(memberRepository.save(any()))
                .willThrow(new DataIntegrityViolationException("some text"));

        // when & then
        assertThatThrownBy(() -> memberService.createMember(dto))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.SIGNUP_FAIL);
    }

    @Test
    void getMemberIdByUsername_success() {
        // given
        String username = "usernmame";

        given(memberRepository.findByUsername(username)).willReturn(Optional.of(mockMember));
        given(mockMember.getId()).willReturn(1L);

        // when
        Long id = memberService.getMemberIdByUsername(username);

        // then
        assertThat(id).isEqualTo(1L);
        then(memberRepository).should().findByUsername(username);
    }

    @Test
    void getMemberIdByUsername_fail_unknownUsername() {
        // given
        String username = "usernmame";

        given(memberRepository.findByUsername(username)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.getMemberIdByUsername(username))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.USER_NOT_FOUND);

        then(mockMember).shouldHaveNoInteractions();
    }
}
