package com.studypals.domain.studyManage.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.entity.StudyCategory;

/**
 *
 * @author jack8
 * @since 2025-04-10
 */
@DataJpaTest
@DisplayName("StudyCategory_JPA_test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StudyCategoryRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private StudyCategoryRepository studyCategoryRepository;

    private Member insertMember() {
        return em.persist(Member.builder()
                .username("username")
                .password("password")
                .nickname("nickname")
                .build());
    }

    private StudyCategory make(Member member, String name) {
        return StudyCategory.builder()
                .name(name)
                .member(member)
                .color("color")
                .dayBelong(12)
                .description("description")
                .build();
    }

    @Test
    public void findByMemberId_success() {
        // given
        Member member = insertMember();
        List<StudyCategory> categories = IntStream.range(1, 10)
                .mapToObj(i -> make(member, "category " + i))
                .toList();
        List<String> expectedName =
                categories.stream().map(StudyCategory::getName).toList();
        studyCategoryRepository.saveAll(categories);

        // when
        List<StudyCategory> finded = studyCategoryRepository.findByMemberId(member.getId());

        // then
        List<String> actualName = finded.stream().map(StudyCategory::getName).toList();
        assertThat(actualName).containsExactlyInAnyOrderElementsOf(expectedName);
    }

    @Test
    public void deleteByMemberId_success() {
        // given
        Member member = insertMember();
        IntStream.range(1, 10).mapToObj(i -> make(member, "category " + i)).forEach(category -> em.persist(category));

        // when
        studyCategoryRepository.deleteByMemberId(member.getId());

        // then
        assertThat(em.getEntityManager()
                        .createQuery("SELECT c FROM StudyCategory c WHERE c.member.id = :memberId", StudyCategory.class)
                        .setParameter("memberId", member.getId())
                        .getResultList())
                .isEmpty();
    }
}
