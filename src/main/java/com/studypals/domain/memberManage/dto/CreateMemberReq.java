package com.studypals.domain.memberManage.dto;

import com.studypals.domain.memberManage.entity.Member;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import org.springframework.jdbc.support.CustomSQLErrorCodesTranslation;

import java.time.LocalDate;

/**
 * 회원가입 시 사용되는 DTO 입니다.
 * <p>
 * record 를 사용하였으며 Validaton 어노테이션을 통해 최소한의 검증이 이루어집니다.
 *
 * @author jack8
 * @since 2025-04-02
 */
public record CreateMemberReq(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String nickname,
        @PastOrPresent LocalDate birthday,
        String position,
        String imageUrl
        ) {

        public Member toEntity(String password) {
                return Member.builder()
                        .username(this.username)
                        .password(password)
                        .nickname(this.nickname)
                        .birthday(this.birthday)
                        .position(this.position)
                        .imageUrl(this.imageUrl)
                        .build();
        }
}
