package com.studypals.domain.studyManage.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.PersistenceException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.entity.DailyStudyInfo;
import com.studypals.testModules.testSupport.DataJpaSupport;

/**
 * {@link  DailyStudyInfoRepository} 에 대한 테스트코드입니다.
 *
 * @author jack8
 * @since 2025-04-19
 */
@DisplayName("DailyStudyInfo_JPA_test")
class DailyStudyInfoRepositoryTest extends DataJpaSupport {

    @Autowired
    private DailyStudyInfoRepository dailyStudyInfoRepository;

    private DailyStudyInfo make(Member member, LocalDate studiedDate, LocalTime startTime, LocalTime endTime) {
        return DailyStudyInfo.builder()
                .member(member)
                .studiedDate(studiedDate)
                .startTime(startTime)
                .endTime(endTime)
                .build();
    }

    @Test
    void save_fail_uniqueConstraint() {
        Member member = insertMember();
        LocalDate date = LocalDate.of(1999, 8, 20);
        LocalTime time = LocalTime.of(10, 30);

        em.persist(make(member, date, time, time.plusHours(2)));
        em.persist(make(member, date, time.plusHours(2), time.plusHours(4)));

        assertThrows(PersistenceException.class, () -> em.flush());
        em.clear();
    }

    @Test
    void findByMemberIdAndStudiedDate_success() {
        // given
        Member member = insertMember();
        LocalDate date = LocalDate.of(1999, 8, 20);
        LocalTime time = LocalTime.of(10, 30);

        em.persist(make(member, date, time, time.plusHours(2)));

        em.flush();
        em.clear();

        // when
        Optional<DailyStudyInfo> result = dailyStudyInfoRepository.findByMemberIdAndStudiedDate(member.getId(), date);

        // then
        assertThat(result).isNotEmpty();
    }

    @Test
    void existsByMemberIdAndStudiedDate_success_returnTrue() {
        // gvien
        Member member = insertMember();
        LocalDate date = LocalDate.of(1999, 8, 20);
        LocalTime time = LocalTime.of(10, 30);

        em.persist(make(member, date, time, time.plusHours(2)));
        em.persist(make(member, date.minusDays(1), time, time.plusHours(2)));
        em.persist(make(member, date.plusDays(1), time, time.plusHours(2)));

        em.flush();
        em.clear();

        // when
        boolean result = dailyStudyInfoRepository.existsByMemberIdAndStudiedDate(member.getId(), date);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void existsByMemberIdAndStudiedDate_success_returnFalse() {
        // gvien
        Member member = insertMember();
        LocalDate date = LocalDate.of(1999, 8, 20);
        LocalTime time = LocalTime.of(10, 30);

        em.persist(make(member, date, time, time.plusHours(2)));
        em.persist(make(member, date.plusDays(1), time, time.plusHours(2)));

        em.flush();
        em.clear();

        // when
        boolean result = dailyStudyInfoRepository.existsByMemberIdAndStudiedDate(member.getId(), date);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void findAllByMemberIdAndStudiedDateBetween_success() {
        // given
        Member member = insertMember();
        LocalDate date = LocalDate.of(1999, 8, 20);
        LocalTime time = LocalTime.of(10, 30);

        em.persist(make(member, date, time, time.plusHours(2)));
        em.persist(make(member, date.plusDays(1), time, time.plusHours(2)));
        em.persist(make(member, date.plusDays(2), time, time.plusHours(2)));
        em.persist(make(member, date.plusDays(3), time, time.plusHours(2)));
        em.persist(make(member, date.plusDays(4), time, time.plusHours(2)));

        em.flush();
        em.clear();

        // when
        List<DailyStudyInfo> results =
                dailyStudyInfoRepository.findAllByMemberIdAndStudiedDateBetween(member.getId(), date, date.plusDays(5));

        // then
        assertThat(results).hasSize(5);
    }
}
