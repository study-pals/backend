package com.studypals.domain.studyManage.dao;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import com.studypals.domain.memberManage.entity.Member;
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
                .studyType(StudyType.TEMPORARY)
                .name(temporaryName)
                .member(member)
                .studiedDate(date)
                .time(time)
                .build();
    }

    private StudyTime make(Member member, Long typeId, LocalDate date, Long time) {
        return StudyTime.builder()
                .studyType(StudyType.PERSONAL)
                .typeId(typeId)
                .member(member)
                .studiedDate(date)
                .time(time)
                .build();
    }

    @Test
    void save_fail_bothNameNull() {
        // given
        Member member = insertMember();
        LocalDate date = LocalDate.of(1999, 8, 20);
        StudyTime studyTime =
                StudyTime.builder().member(member).studiedDate(date).build();

        // when & than
        assertThatThrownBy(() -> studyTimeRepository.save(studyTime))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("must have value temporary name or typeId");
    }

    @Test
    void findByMemberIdAndStudiedDate_success() {
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
        List<StudyTime> results = studyTimeRepository.findByMemberIdAndStudiedDate(member.getId(), date);

        // then
        assertThat(results)
                .hasSize(3)
                .extracting(StudyTime::getName)
                .containsExactlyInAnyOrder("temp1", "temp2", "temp3");
    }

    @Test
    void findAllByMemberIdAndstudiedDateBetween_returnsAllInRange() {
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

    @Test
    void findByStudyType_success() {
        // given
        Member member = insertMember();
        LocalDate march = LocalDate.of(2024, 3, 1);
        Long typeId = 1L;

        em.persist(make(member, typeId, march, 100L));

        em.flush();
        em.clear();

        // when
        Optional<StudyTime> result =
                studyTimeRepository.findByStudyType(member.getId(), march, StudyType.PERSONAL.name(), typeId);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get().getTime()).isEqualTo(100L);
    }

    @Test
    void findByTemporaryName_success() {
        // given
        Member member = insertMember();
        LocalDate march = LocalDate.of(2024, 3, 1);
        String name = "temporary name";

        em.persist(make(member, name, march, 100L));

        em.flush();
        em.clear();

        // when
        Optional<StudyTime> result = studyTimeRepository.findByName(member.getId(), march, name);
    }
}
