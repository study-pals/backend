package com.studypals.domain.studyManage.dao;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.entity.DateType;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyTime;
import com.studypals.domain.studyManage.entity.StudyType;
import com.studypals.testModules.testSupport.DataJpaSupport;

/**
 * {@link StudyTimeRepository} 에 대한 테스트 클래스
 *
 * @author jack8
 * @since 2025-04-10
 */
@DisplayName("StudyTime_JPA_test")
class StudyTimeRepositoryTest extends DataJpaSupport {

    @Autowired
    private StudyTimeRepository studyTimeRepository;

    private StudyCategory insertCategory(Long userId, int cnt) {
        return em.persist(StudyCategory.builder()
                .studyType(StudyType.PERSONAL)
                .goal(3600L)
                .color("#FFFFF")
                .dateType(DateType.DAILY)
                .dayBelong(127)
                .name("category" + cnt)
                .typeId(userId)
                .description("description")
                .build());
    }

    private StudyTime make(Member member, String temporaryName, LocalDate date, Long time) {
        return StudyTime.builder()
                .name(temporaryName)
                .member(member)
                .studiedDate(date)
                .time(time)
                .build();
    }

    private StudyTime make(Member member, StudyCategory category, LocalDate date, Long time) {
        return StudyTime.builder()
                .studyCategory(category)
                .member(member)
                .studiedDate(date)
                .time(time)
                .build();
    }

    @Test
    void findByMemberIdAndStudiedDate_success() {
        // given
        Member member = insertMember();
        StudyCategory studyCategory1 = insertCategory(member.getId(), 1);
        StudyCategory studyCategory2 = insertCategory(member.getId(), 2);
        StudyCategory studyCategory3 = insertCategory(member.getId(), 3);
        LocalDate date = LocalDate.of(2024, 4, 10);

        em.persist(make(member, studyCategory1, date, 100L));
        em.persist(make(member, studyCategory2, date, 110L));
        em.persist(make(member, studyCategory3, date, 120L));
        em.persist(make(member, "temp4", date.plusDays(1), 100L)); // 다른 날

        em.flush();
        em.clear();

        // when
        List<StudyTime> results = studyTimeRepository.findByMemberIdAndStudiedDate(member.getId(), date);

        // then
        assertThat(results)
                .hasSize(3)
                .extracting(StudyTime::getStudyCategory)
                .extracting(StudyCategory::getName)
                .containsExactlyInAnyOrder("category1", "category2", "category3");
    }

    @Test
    void findAllByMemberIdAndStudiedDateBetween_returnsAllInRange() {
        // given
        Member member = insertMember();
        LocalDate april = LocalDate.of(2024, 4, 1);

        em.persist(make(member, "day1", april, 100L));
        em.persist(make(member, "day2", april.plusDays(1), 100L));
        em.persist(make(member, "day3", april.plusDays(2), 100L));
        em.persist(make(member, "day4", april.plusDays(10), 100L));
        em.persist(make(member, "day5", april.plusDays(20), 100L));
        em.persist(make(member, "day6", april.plusDays(29), 100L));

        em.flush();
        em.clear();

        // when
        List<StudyTime> results =
                studyTimeRepository.findAllByMemberIdAndStudiedDateBetween(member.getId(), april, april.plusDays(30));

        // then
        assertThat(results).hasSize(6);
    }

    @Test
    void findAllByMemberIdAndStudiedDateBetween_success_return0() {
        // given
        Member member = insertMember();
        LocalDate march = LocalDate.of(2024, 3, 1);

        em.flush();
        em.clear();

        // when
        List<StudyTime> results =
                studyTimeRepository.findAllByMemberIdAndStudiedDateBetween(member.getId(), march, march.plusDays(30));

        // then
        assertThat(results).isEmpty();
    }
}
