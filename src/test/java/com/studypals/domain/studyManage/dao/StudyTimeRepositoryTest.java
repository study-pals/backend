package com.studypals.domain.studyManage.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.entity.StudyTime;

/**
 * {@link StudyTimeRepository} 에 대한 테스트 클래스
 *
 * @author jack8
 * @since 2025-04-10
 */
@DataJpaTest
@DisplayName("StudyTime_JPA_test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StudyTimeRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private StudyTimeRepository studyTimeRepository;

    private Member insertMember() {
        return em.persist(Member.builder()
                .username("username")
                .password("password")
                .nickname("nickname")
                .build());
    }

    private StudyTime make(Member member, String temporaryName, LocalDate date, Long time) {
        return StudyTime.builder()
                .temporaryName(temporaryName)
                .member(member)
                .studiedAt(date)
                .time(time)
                .build();
    }

    @Test
    void save_fail_bothNameNull() {
        // given
        Member member = insertMember();
        LocalDate date = LocalDate.of(1999, 8, 20);
        StudyTime studyTime = StudyTime.builder().member(member).studiedAt(date).build();

        // when & than
        assertThatThrownBy(() -> studyTimeRepository.save(studyTime))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("must have value temporary name or category");
    }

    @Test
    void findByMemberIdAndStudiedAt_success() {
        // given
        Member member = insertMember();
        LocalDate date = LocalDate.of(2024, 4, 10);

        em.persist(make(member, "temp1", date, 100L));
        em.persist(make(member, "temp2", date, 100L));
        em.persist(make(member, "temp3", date, 100L));
        em.persist(make(member, "temp4", date.plusDays(1), 100L)); // 다른 날

        em.flush();
        em.clear();

        // when
        List<StudyTime> results = studyTimeRepository.findByMemberIdAndStudiedAt(member.getId(), date);

        // then
        assertThat(results)
                .hasSize(3)
                .extracting(StudyTime::getTemporaryName)
                .containsExactlyInAnyOrder("temp1", "temp2", "temp3");
    }

    @Test
    void sumTimeByMemberAndDate_success() {
        // given
        Member member = insertMember();
        LocalDate date = LocalDate.of(2024, 4, 10);

        em.persist(make(member, "t1", date, 30L));
        em.persist(make(member, "t2", date, 45L));
        em.persist(make(member, "t3", date, 10L));

        em.flush();
        em.clear();

        // when
        Long total = studyTimeRepository.sumTimeByMemberAndDate(member.getId(), date);

        // then
        assertThat(total).isEqualTo(85L);
    }

    @Test
    void sumTimeByMemberAndDate_success_return0() {
        // given
        Member member = insertMember();
        LocalDate emptyDate = LocalDate.of(2024, 4, 11);

        em.flush();
        em.clear();

        // when
        Long result = studyTimeRepository.sumTimeByMemberAndDate(member.getId(), emptyDate);

        // then
        assertThat(result).isEqualTo(0L);
    }

    @Test
    @DisplayName("findByMemberIdAndStudiedAtBetween - 특정 기간 내 데이터 조회")
    void findByMemberIdAndStudiedAtBetween_returnsAllInRange() {
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
                studyTimeRepository.findByMemberIdAndStudiedAtBetween(member.getId(), april, april.plusDays(30));

        // then
        assertThat(results).hasSize(6);
    }

    @Test
    void findByMemberIdAndStudiedAtBetween_success_return0() {
        // given
        Member member = insertMember();
        LocalDate march = LocalDate.of(2024, 3, 1);

        em.flush();
        em.clear();

        // when
        List<StudyTime> results =
                studyTimeRepository.findByMemberIdAndStudiedAtBetween(member.getId(), march, march.plusDays(30));

        // then
        assertThat(results).isEmpty();
    }
}
