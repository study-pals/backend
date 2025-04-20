package com.studypals.domain.studyManage.dto.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.dto.GetDailyStudyInfoDto;
import com.studypals.domain.studyManage.entity.DailyStudyInfo;

/**
 * {@link DailyStudyInfoMapper} 에 대한 테스트코드
 *
 * @author jack8
 * @since 2025-04-20
 */
class DailyStudyInfoMapperTest {

    private final DailyStudyInfoMapper mapper = Mappers.getMapper(DailyStudyInfoMapper.class);

    @Test
    @DisplayName("DailyStudyInfo → GetDailyStudyInfoDto 매핑 성공")
    void toDto_success() {
        // given
        DailyStudyInfo entity = DailyStudyInfo.builder()
                .id(1L)
                .member(Member.builder().id(1L).build())
                .studiedAt(LocalDate.of(2024, 4, 20))
                .startAt(LocalTime.of(9, 0))
                .endAt(LocalTime.of(12, 30))
                .memo("집중 잘 됨")
                .build();

        // when
        GetDailyStudyInfoDto dto = mapper.toDto(entity);

        // then
        assertThat(dto.studiedAt()).isEqualTo(LocalDate.of(2024, 4, 20));
        assertThat(dto.startAt()).isEqualTo(LocalTime.of(9, 0));
        assertThat(dto.endAt()).isEqualTo(LocalTime.of(12, 30));
        assertThat(dto.memo()).isEqualTo("집중 잘 됨");
    }
}
