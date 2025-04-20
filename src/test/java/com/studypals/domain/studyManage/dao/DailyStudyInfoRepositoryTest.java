package com.studypals.domain.studyManage.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.entity.DailyStudyInfo;

/**
 * {@link  DailyStudyInfoRepository} 에 대한 테스트코드입니다.
 *
 * @author jack8
 * @since 2025-04-19
 */
@DataJpaTest
@DisplayName("DailyStudyInfo_JPA_test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DailyStudyInfoRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private DailyStudyInfoRepository dailyStudyInfoRepository;

    private Member insertMember() {
        return em.persist(Member.builder()
                .username("username")
                .password("password")
                .nickname("nickname")
                .build());
    }

    private DailyStudyInfo make(Member member, LocalDate studiedDate, LocalTime startTime, LocalTime endTime) {
        return DailyStudyInfo.builder()
                .member(member)
                .studiedDate(studiedDate)
                .startTime(startTime)
                .endTime(endTime)
                .build();
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
