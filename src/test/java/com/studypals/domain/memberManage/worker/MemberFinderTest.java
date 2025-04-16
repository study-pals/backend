package com.studypals.domain.memberManage.worker;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.memberManage.dao.MemberRepository;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.global.exceptions.errorCode.AuthErrorCode;
import com.studypals.global.exceptions.exception.AuthException;

/**
 * {@link MemberFinder} 에 대한 단위 테스트 코드
 *
 * @author jack8
 * @since 2025-04-16
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MemberFinder 유닛 테스트")
class MemberFinderTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private Member mockMember;

    @InjectMocks
    private MemberFinder memberFinder;

    @Test
    void findMemberById_success() {
        // given
        given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember));

        // when
        Member result = memberFinder.findMember(1L);

        // then
        assertThat(result).isEqualTo(mockMember);
    }

    @Test
    void findMemberById_fail_userNotFound() {
        // given
        given(memberRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberFinder.findMember(1L))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.USER_NOT_FOUND);
    }

    @Test
    void findMemberByUsername_success() {
        // given
        given(memberRepository.findByUsername("username")).willReturn(Optional.of(mockMember));

        // when
        Member result = memberFinder.findMember("username");

        // then
        assertThat(result).isEqualTo(mockMember);
    }

    @Test
    void findMemberByUsername_fail_userNotFound() {
        // given
        given(memberRepository.findByUsername("username")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberFinder.findMember("username"))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.USER_NOT_FOUND);
    }

    @Test
    void findMemberRef_success() {
        // given
        given(memberRepository.getReferenceById(1L)).willReturn(mockMember);

        // when
        Member result = memberFinder.findMemberRef(1L);

        // then
        assertThat(result).isEqualTo(mockMember);
    }
}
