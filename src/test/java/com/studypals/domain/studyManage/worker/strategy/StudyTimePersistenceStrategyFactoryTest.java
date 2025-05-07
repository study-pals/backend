package com.studypals.domain.studyManage.worker.strategy;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.studypals.domain.studyManage.entity.StudyStatus;

/**
 * {@link StudyTimePersistenceStrategyFactory} 에 대한 테스트 코드
 * 전략 패턴에 의하여 전략 객체가 정상적으로 매핑되는지에 대한 테스트
 *
 * @author jack8
 * @since 2025-05-07
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StudyTimePersistenceStrategyFactoryTest {

    @Mock
    PersonalStudyPersistenceStrategy personalStrategy;

    @Mock
    GroupStudyPersistenceStrategy groupStrategy;

    @Mock
    TemporarStudyPersistenceStrategy temporarStrategy;

    @Mock
    StudyStatus personalStatus;

    @Mock
    StudyStatus groupStatus;

    @Mock
    StudyStatus temporarStatus;

    @InjectMocks
    StudyTimePersistenceStrategyFactory strategyFactory;

    @BeforeEach
    void setUp() {
        given(personalStrategy.supports(personalStatus)).willReturn(true);
        given(groupStrategy.supports(groupStatus)).willReturn(true);
        given(temporarStrategy.supports(temporarStatus)).willReturn(true);

        strategyFactory =
                new StudyTimePersistenceStrategyFactory(List.of(personalStrategy, groupStrategy, temporarStrategy));
    }

    @Test
    void resolve_returnsPersonalStrategy() {
        StudyTimePersistenceStrategy resolved = strategyFactory.resolve(personalStatus);
        assertThat(resolved).isEqualTo(personalStrategy);
    }

    @Test
    void resolve_returnsGroupStrategy() {
        StudyTimePersistenceStrategy resolved = strategyFactory.resolve(groupStatus);
        assertThat(resolved).isEqualTo(groupStrategy);
    }

    @Test
    void resolve_returnsCompanyStrategy() {
        StudyTimePersistenceStrategy resolved = strategyFactory.resolve(temporarStatus);
        assertThat(resolved).isEqualTo(temporarStrategy);
    }
}
