package com.studypals.domain.studyManage.dto.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.studypals.domain.studyManage.dto.CreateCategoryDto;
import com.studypals.domain.studyManage.dto.CreateCategoryReq;
import com.studypals.domain.studyManage.entity.DateType;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyType;

/**
 * {@link CategoryMapper} 에 대한 테스트
 *
 * @author jack8
 * @since 2025-04-20
 */
class CategoryMapperTest {

    private final CategoryMapper mapper = Mappers.getMapper(CategoryMapper.class);

    @Test
    @DisplayName("CreateCategoryReq + Member → PersonalStudyCategory 매핑 성공")
    void toEntity_success() {
        // given
        CreateCategoryReq req = new CreateCategoryReq(null, "알고리즘", DateType.DAILY, 1200L, "#FFEEAA", 12, "설명입니다.");

        // when
        CreateCategoryDto dto = mapper.reqToDto(req, StudyType.PERSONAL, 1L);
        StudyCategory entity = mapper.toEntity(dto);

        // then
        assertThat(entity.getId()).isNull(); // ignore 설정
        assertThat(entity.getName()).isEqualTo("알고리즘");
        assertThat(entity.getColor()).isEqualTo("#FFEEAA");
        assertThat(entity.getDayBelong()).isEqualTo(12);
        assertThat(entity.getDescription()).isEqualTo("설명입니다.");
    }
}
