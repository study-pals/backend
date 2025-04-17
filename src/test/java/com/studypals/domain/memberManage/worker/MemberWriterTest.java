package com.studypals.domain.memberManage.worker;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.studypals.domain.memberManage.dao.MemberRepository;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.global.exceptions.errorCode.AuthErrorCode;
import com.studypals.global.exceptions.exception.AuthException;

/**
 * {@link MemberWriter} 에 대한 단위 테스트
 *
 * @author jack8
 * @since 2025-04-16
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MemberWriter 유닛 테스트")
class MemberWriterTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private Member mockMember;

    @InjectMocks
    private MemberWriter memberWriter;

    @Test
    void save_success() {

        // when & then
        assertThatCode(() -> memberWriter.save(mockMember)).doesNotThrowAnyException();
    }

    @Test
    void save_fail_duplicate() {
        // given
        willThrow(new DataIntegrityViolationException("duplicate"))
                .given(memberRepository)
                .save(mockMember);

        // when & then
        assertThatThrownBy(() -> memberWriter.save(mockMember))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.SIGNUP_FAIL);
    }
}
