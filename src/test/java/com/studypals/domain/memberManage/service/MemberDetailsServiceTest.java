package com.studypals.domain.memberManage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.studypals.domain.memberManage.dao.MemberRepository;
import com.studypals.domain.memberManage.entity.Member;

/**
 * {@link MemberDetailsService} 에 대한 테스트코드입니다.
 *
 * <p>성공 케이스 및 존재하지 않는 유저 시 뱉는 예외에 대한 테스트입니다.
 *
 * @author jack8
 * @see MemberDetailsService
 * @since 2025-04-07
 */
@ExtendWith(MockitoExtension.class)
class MemberDetailsServiceTest {
    @Mock private MemberRepository memberRepository;
    @Mock private Member mockMember;

    @InjectMocks private MemberDetailsService memberDetailsService;

    @Test
    void loadUserByUsername_success() {
        // given
        String username = "username";
        given(memberRepository.findByUsername(username)).willReturn(Optional.of(mockMember));
        given(mockMember.getUsername()).willReturn(username);

        // when
        UserDetails userDetails = memberDetailsService.loadUserByUsername(username);

        // then
        then(memberRepository).should().findByUsername(username);
        assertThat(userDetails.getUsername()).isEqualTo(username);
    }

    @Test
    void loadUserByUsername_fail_unknown_user() {
        // given
        String username = "username";
        given(memberRepository.findByUsername(username)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberDetailsService.loadUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("invalid username");

        then(memberRepository).should().findByUsername(username);
    }
}
