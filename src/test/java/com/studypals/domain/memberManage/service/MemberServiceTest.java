package com.studypals.domain.memberManage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.studypals.domain.memberManage.dto.CreateMemberReq;
import com.studypals.domain.memberManage.dto.mappers.MemberMapper;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.worker.MemberReader;
import com.studypals.domain.memberManage.worker.MemberWriter;
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

    @Mock
    private MemberReader memberReader;

    @Mock
    private MemberWriter memberWriter;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MemberMapper mapper;

    @Mock
    private Member mockMember;

    @InjectMocks
    private MemberServiceImpl memberService;

    @Test
    void createMember_success() {
        // given
        CreateMemberReq dto = new CreateMemberReq(
                "username", "password", "nickname", LocalDate.of(1999, 8, 20), "student", "example.com");

        given(passwordEncoder.encode("password")).willReturn("encoded password");

        given(mapper.toEntity(dto, "encoded password")).willReturn(mockMember);
        given(mockMember.getId()).willReturn(1L);

        // when
        Long id = memberService.createMember(dto);

        // then
        assertThat(id).isEqualTo(1L);
        then(passwordEncoder).should().encode("password");
    }

    @Test
    void createMember_fail_dupliateUser() {
        // given
        CreateMemberReq dto = new CreateMemberReq(
                "username", "password", "nickname", LocalDate.of(1999, 8, 20), "student", "example.com");

        given(passwordEncoder.encode("password")).willReturn("encoded password");
        willThrow(new AuthException(AuthErrorCode.SIGNUP_FAIL))
                .given(memberWriter)
                .save(any());
        given(mapper.toEntity(dto, "encoded password")).willReturn(mockMember);

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

        given(memberReader.get(username)).willReturn(mockMember);
        given(mockMember.getId()).willReturn(1L);

        // when
        Long id = memberService.getMemberIdByUsername(username);

        // then
        assertThat(id).isEqualTo(1L);
    }

    @Test
    void getMemberIdByUsername_fail_unknownUsername() {
        // given
        String username = "usernmame";

        given(memberReader.get(username)).willThrow(new AuthException(AuthErrorCode.USER_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> memberService.getMemberIdByUsername(username))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.USER_NOT_FOUND);

        then(mockMember).shouldHaveNoInteractions();
    }
}
