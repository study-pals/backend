package com.studypals.domain.studyManage.dto.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.dto.GetStudyDto;
import com.studypals.domain.studyManage.dto.StartStudyRes;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyStatus;
import com.studypals.domain.studyManage.entity.StudyTime;

/**
 * {@link StudyTimeMapper} 에 대한 테스트
 *
 * @author jack8
 * @since 2025-04-20
 */
class StudyTimeMapperTest {
    private final StudyTimeMapper mapper = Mappers.getMapper(StudyTimeMapper.class);

    @Test
    @DisplayName("StudyStatus → StartStudyRes 매핑 성공")
    void toDto_success_studyStatusToStartStudyRes() {
        // given
        LocalDateTime startTime = LocalDateTime.of(2025, 8, 20, 10, 30);
        StudyStatus entity = StudyStatus.builder()
                .id(1L)
                .studyTime(100L)
                .startTime(startTime)
                .categoryId(1L)
                .name("temp")
                .build();

        // when
        StartStudyRes dto = mapper.toDto(entity);

        // then
        assertThat(dto.studying()).isTrue();
        assertThat(dto.startTime()).isEqualTo(startTime);
        assertThat(dto.studyTime()).isEqualTo(100L);
        assertThat(dto.categoryId()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("temp");
    }

    @Test
    @DisplayName("StudyTime → GetStudyDto 매핑 성공")
    void toDto_success_studyTimeToGetStudyDto() {
        // given
        StudyCategory category =
                StudyCategory.builder().id(7L).name("algorithm").build();

        StudyTime studyTime = StudyTime.builder()
                .id(1L)
                .name("temp")
                .studiedDate(LocalDate.of(2024, 1, 1))
                .time(80L)
                .member(Member.builder().id(1L).build())
                .build();

        // when
        GetStudyDto dto = mapper.toDto(studyTime);

        // then
        assertThat(dto.name()).isEqualTo("temp");
        assertThat(dto.time()).isEqualTo(80L);
    }
}
