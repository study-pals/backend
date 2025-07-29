package com.studypals.domain.studyManage.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.entity.PersonalStudyCategory;
import com.studypals.testModules.testSupport.DataJpaSupport;

/**
 *
 * @author jack8
 * @since 2025-04-10
 */
@DisplayName("StudyCategory_JPA_test")
class PersonalPersonalStudyCategoryRepositoryTest extends DataJpaSupport {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private PersonalStudyCategoryRepository personalStudyCategoryRepository;

    private Member insertMember() {
        return em.persist(Member.builder()
                .username("username")
                .password("password")
                .nickname("nickname")
                .build());
    }

    private PersonalStudyCategory make(Member member, String name) {
        return PersonalStudyCategory.builder()
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
        List<PersonalStudyCategory> categories = IntStream.range(1, 10)
                .mapToObj(i -> make(member, "category " + i))
                .toList();
        List<String> expectedName =
                categories.stream().map(PersonalStudyCategory::getName).toList();
        personalStudyCategoryRepository.saveAll(categories);

        // when
        List<PersonalStudyCategory> finded = personalStudyCategoryRepository.findByMemberId(member.getId());

        // then
        List<String> actualName =
                finded.stream().map(PersonalStudyCategory::getName).toList();
        assertThat(actualName).containsExactlyInAnyOrderElementsOf(expectedName);
    }

    @Test
    public void deleteByMemberId_success() {
        // given
        Member member = insertMember();
        IntStream.range(1, 10).mapToObj(i -> make(member, "category " + i)).forEach(category -> em.persist(category));

        // when
        personalStudyCategoryRepository.deleteByMemberId(member.getId());

        // then
        assertThat(em.getEntityManager()
                        .createQuery(
                                "SELECT c FROM PersonalStudyCategory c WHERE c.member.id = :memberId",
                                PersonalStudyCategory.class)
                        .setParameter("memberId", member.getId())
                        .getResultList())
                .isEmpty();
    }
}
