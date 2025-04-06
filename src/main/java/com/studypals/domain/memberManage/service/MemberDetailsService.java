package com.studypals.domain.memberManage.service;

import com.studypals.domain.memberManage.dao.MemberRepository;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.entity.MemberDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 스프링 시큐리티에게 유저 정보를 건내주는 역할을 위임받은 객체입니다.
 * <p>
 * loadUserByUsername 메서드를 통하여 MemberDetails를 반환합니다.
 *
 * <p><b>상속 정보:</b><br>
 * UserDetailsService 를 상속받습니다. 해당 인터페이스의 구현체로서, 빈에 등록되어 추후
 * spring seucurity가 이를 호출하면 해당 객체가 주입됩니다.
 *
 *
 * @author jack8
 * @see MemberDetails
 * @since 2025-04-02
 */
@Service
@RequiredArgsConstructor
public class MemberDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("invalid username")
        );
        return new MemberDetails(member);
    }
}
