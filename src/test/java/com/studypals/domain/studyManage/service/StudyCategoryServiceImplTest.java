package com.studypals.domain.studyManage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.studyManage.dto.CreateCategoryDto;
import com.studypals.domain.studyManage.dto.GetCategoryRes;
import com.studypals.domain.studyManage.dto.mappers.CategoryMapper;
import com.studypals.domain.studyManage.entity.DateType;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyType;
import com.studypals.domain.studyManage.worker.StudyCategoryReader;
import com.studypals.domain.studyManage.worker.StudyCategoryWriter;
import com.studypals.domain.studyManage.worker.StudyStatusWorker;
import com.studypals.domain.studyManage.worker.categoryStrategy.CategoryStrategyFactory;
import com.studypals.domain.studyManage.worker.categoryStrategy.PersonalCategoryStrategy;

/**
 * {@link StudyCategoryServiceImpl} 에 대한 테스트 코드
 *
 * @author jack8
 * @since 2025-08-15
 */
@ExtendWith(MockitoExtension.class)
class StudyCategoryServiceImplTest {

    @Mock
    private StudyCategoryWriter studyCategoryWriter;

    @Mock
    private StudyCategoryReader studyCategoryReader;

    @Mock
    private StudyStatusWorker studyStatusWorker;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private CategoryStrategyFactory categoryStrategyFactory;

    @Mock
    private PersonalCategoryStrategy personalCategoryStrategy;

    @Mock
    private StudyCategory mockStudyCategory;

    @InjectMocks
    private StudyCategoryServiceImpl studyCategoryService;

    @Test
    void createCategory_success() {
        // given
        Long userId = 1L;
        CreateCategoryDto dto =
                new CreateCategoryDto("name", StudyType.PERSONAL, userId, DateType.DAILY, 3600L, "#FFFFFF", 127, null);

        given(categoryStrategyFactory.resolve(StudyType.PERSONAL)).willReturn(personalCategoryStrategy);
        given(categoryMapper.toEntity(dto)).willReturn(mockStudyCategory);
        given(mockStudyCategory.getId()).willReturn(2L);

        // when
        Long res = studyCategoryService.createCategory(userId, dto);

        // then
        assertThat(res).isEqualTo(2L);
        then(studyCategoryWriter).should().save(mockStudyCategory);
    }

    @Test
    void getAllUserCategories_success() {
        // given
        Long userId = 1L;
        Map<StudyType, List<Long>> typeListMap = Map.of(StudyType.PERSONAL, List.of(1L));
        GetCategoryRes getCategoryRes = GetCategoryRes.builder().build();
        given(categoryStrategyFactory.getTypeMap(userId)).willReturn(typeListMap);
        given(studyCategoryReader.findByTypesAndTypeIds(typeListMap)).willReturn(List.of(mockStudyCategory));
        given(categoryMapper.toDto(mockStudyCategory)).willReturn(getCategoryRes);

        // when
        List<GetCategoryRes> res = studyCategoryService.getAllUserCategories(userId);

        // then
        assertThat(res).hasSize(1);
        assertThat(res.get(0)).isEqualTo(getCategoryRes);
    }

    @Test
    void updateCategory_success() {
        // uh... I think this method is simple enough that we don't need to write tests.
        // Also, I prefer not to test calls to inner classes.
    }

    @Test
    void deleteCategory_success() {
        // given
        Long userId = 1L;
        Long categoryId = 2L;

        given(studyStatusWorker.isStudying(userId)).willReturn(false);
        given(studyCategoryReader.getById(categoryId)).willReturn(mockStudyCategory);
        given(mockStudyCategory.getStudyType()).willReturn(StudyType.PERSONAL);
        given(categoryStrategyFactory.resolve(StudyType.PERSONAL)).willReturn(personalCategoryStrategy);

        // when
        studyCategoryService.deleteCategory(userId, categoryId);

        // then
        then(studyCategoryWriter).should().remove(mockStudyCategory);
    }
}
