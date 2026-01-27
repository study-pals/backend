package com.studypals.testModules.testSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.entity.MemberProfileImage;
import com.studypals.global.config.QueryDslTestConfig;

@DataJpaTest
@Import(QueryDslTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class DataJpaSupport extends TestEnvironment {
    @Autowired
    protected TestEntityManager em;

    protected Member insertMember() {
        return em.persist(Member.builder()
                .username("username")
                .password("password")
                .nickname("nickname")
                .build());
    }

    protected Member insertMember(String username, String nickname) {
        Member member = em.persist(Member.builder()
                .username(username)
                .password("password")
                .nickname(nickname)
                .build());

        member.setProfileImage(insertMemberProfileImage(member));

        return member;
    }

    protected MemberProfileImage insertMemberProfileImage(Member member) {
        return em.persist(MemberProfileImage.builder()
                .member(member)
                .objectKey("profile/default" + member.getId() + ".jpg")
                .originalFileName("default.jpg")
                .mimeType("jpg")
                .build());
    }
}
