package com.studypals.domain.studyManage.facade.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.studyManage.dto.GetCategoryRes;
import com.studypals.domain.studyManage.dto.GetStudyDto;
import com.studypals.domain.studyManage.dto.GetStudyRes;
import com.studypals.domain.studyManage.entity.StudyType;

/**
 * {@link AbstractCategoryBasedStrategy} 를 상속받는 비슷한 로직의 객체들에 대한 단위 테스트
 *
 * @author jack8
 * @see AbstractCategoryBasedStrategy
 * @see GroupStudyCategoryRenderStrategy
 * @see PersonalCategoryStudyRenderStrategy
 * @since 2025-05-07
 */
@ExtendWith({MockitoExtension.class})
class CategoryBasedStrategyTest {

    private final PersonalCategoryStudyRenderStrategy strategy = new PersonalCategoryStudyRenderStrategy();

    @Test
    void compose_success_matched() {
        // given
        List<GetStudyDto> studies = List.of(
                new GetStudyDto(StudyType.PERSONAL, 1L, null, 60L), // PERSONAL
                new GetStudyDto(StudyType.PERSONAL, 2L, null, 40L));

        List<GetCategoryRes> categories = List.of(
                GetCategoryRes.builder()
                        .typeId(1L)
                        .name("수학")
                        .color("#000000")
                        .description("수학 공부")
                        .build(),
                GetCategoryRes.builder()
                        .typeId(2L)
                        .name("영어")
                        .color("#111111")
                        .description("영어 공부")
                        .build());

        // when
        List<GetStudyRes> result = strategy.compose(studies, categories);

        // then
        assertThat(result).hasSize(2);

        assertThat(result).anySatisfy(res -> {
            assertThat(res.typeId()).isEqualTo(1L);
            assertThat(res.name()).isEqualTo("수학");
            assertThat(res.time()).isEqualTo(60L);
        });

        assertThat(result).anySatisfy(res -> {
            assertThat(res.typeId()).isEqualTo(2L);
            assertThat(res.name()).isEqualTo("영어");
            assertThat(res.time()).isEqualTo(40L);
        });
    }

    @Test
    void compose_success_nonStudied() {
        // given
        List<GetStudyDto> studies = List.of(); // 아무 기록도 없음

        List<GetCategoryRes> categories = List.of(GetCategoryRes.builder()
                .typeId(10L)
                .name("물리")
                .color("#FF0000")
                .description("물리 공부")
                .build());

        // when
        List<GetStudyRes> result = strategy.compose(studies, categories);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).typeId()).isEqualTo(10L);
        assertThat(result.get(0).time()).isEqualTo(0L);
    }
}
