package com.studypals.domain.memberManage.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * spring security 로그인 시 사용할 UserDetails의 구현체입니다.
 * <p>
 * memberDetailsService 의 오버라이드된 메서드에서 반환됩니다.
 *
 * <p><b>상속 정보:</b><br>
 * UserDetails의 구현 클래스이며 Member에 대한 정보를 반환하는 메서드로 구성되어 있습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code MemberDetails(Member member)}  <br>
 * Member를 넣어 생성합니다. <br>

 * @author jack8
 * @see com.studypals.domain.memberManage.service.MemberDetailsService MemberDetailsService
 * @since 2025-04-02
 */
public class MemberDetails implements UserDetails {

    private final Member member;

    public MemberDetails(Member member) {
        this.member = member;
    }
    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getUsername();
    }

    public Long getId() {
        return member.getId();
    }
}
