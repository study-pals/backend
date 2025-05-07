package com.studypals.domain.studyManage.worker.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.dao.StudyTimeRepository;
import com.studypals.domain.studyManage.entity.StudyStatus;
import com.studypals.domain.studyManage.entity.StudyTime;
import com.studypals.domain.studyManage.entity.StudyType;

/**
 * AbstractStudyPersistenceStrategy 를 상속받은 전략 객체에 대한 테스트 코드입니다.
 * 현재 GROUP 과 PERSONAL 의 로직이 동일하므로 PERSONAL 을 사용하여 테스트를 진행합니다.
 *
 * @author jack8
 * @see PersonalStudyPersistenceStrategy
 * @see GroupStudyPersistenceStrategy
 * @see AbstractStudyPersistenceStrategy
 * @since 2025-05-07
 */
@ExtendWith(MockitoExtension.class)
class CategoryBaseStudyPersistenceStrategyTest {

    @Mock
    private StudyTimeRepository studyTimeRepository;

    @Mock
    private Member mockMember;

    @Mock
    private StudyStatus mockStatus;

    @Mock
    private StudyTime mockStudyTime;

    @InjectMocks
    private PersonalStudyPersistenceStrategy personalStudyPersistenceStrategy;

    @Test
    void supports_success() {
        // given
        given(mockStatus.getStudyType()).willReturn(StudyType.PERSONAL);

        // when
        boolean result = personalStudyPersistenceStrategy.supports(mockStatus);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void supports_fail() {
        // given
        given(mockStatus.getStudyType()).willReturn(StudyType.GROUP);

        // when
        boolean result = personalStudyPersistenceStrategy.supports(mockStatus);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void find_success() {
        // given
        Long id = 1L;
        LocalDate date = LocalDate.of(2024, 3, 1);
        given(mockMember.getId()).willReturn(id);
        given(mockStatus.getTypeId()).willReturn(100L);
        given(studyTimeRepository.findByStudyType(id, date, "PERSONAL", 100L)).willReturn(Optional.of(mockStudyTime));

        // when
        Optional<StudyTime> result = personalStudyPersistenceStrategy.find(mockMember, mockStatus, date);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get()).isEqualTo(mockStudyTime);
    }
}
