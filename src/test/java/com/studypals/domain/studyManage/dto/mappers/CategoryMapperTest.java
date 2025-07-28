package com.studypals.domain.studyManage.dto.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.dto.CreateCategoryReq;
import com.studypals.domain.studyManage.entity.StudyCategory;

/**
 * {@link CategoryMapper} 에 대한 테스트
 *
 * @author jack8
 * @since 2025-04-20
 */
class CategoryMapperTest {

    private final CategoryMapper mapper = Mappers.getMapper(CategoryMapper.class);

    @Test
    @DisplayName("CreateCategoryReq + Member → StudyCategory 매핑 성공")
    void toEntity_success() {
        // given
        CreateCategoryReq req = new CreateCategoryReq("알고리즘", 1200L, "#FFEEAA", 12, "설명입니다.");
        Member member = Member.builder().id(1L).build();

        // when
        StudyCategory entity = mapper.toEntity(req, member);

        // then
        assertThat(entity.getId()).isNull(); // ignore 설정
        assertThat(entity.getName()).isEqualTo("알고리즘");
        assertThat(entity.getColor()).isEqualTo("#FFEEAA");
        assertThat(entity.getDayBelong()).isEqualTo(12);
        assertThat(entity.getDescription()).isEqualTo("설명입니다.");
        assertThat(entity.getMember().getId()).isEqualTo(1L);
    }

    //    @Test
    //    @DisplayName("StudyCategory → GetCategoryRes 매핑 성공")
    //    void toDto_success() {
    //        // given
    //        Member member = Member.builder().id(2L).build();
    //        StudyCategory entity = StudyCategory.builder()
    //                .id(10L)
    //                .name("CS")
    //                .color("#CCCCCC")
    //                .dayBelong(7)
    //                .description("CS 공부용 카테고리")
    //                .member(member)
    //                .build();
    //
    //        // when
    //        GetCategoryRes dto = mapper.toDto(entity);
    //
    //        // then
    //        assertThat(dto.categoryId()).isEqualTo(10L);
    //        assertThat(dto.name()).isEqualTo("CS");
    //        assertThat(dto.color()).isEqualTo("#CCCCCC");
    //        assertThat(dto.dayBelong()).isEqualTo(7);
    //        assertThat(dto.description()).isEqualTo("CS 공부용 카테고리");
    //    }
}
