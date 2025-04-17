package com.studypals.domain.studyManage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.worker.MemberReader;
import com.studypals.domain.studyManage.dto.CreateCategoryReq;
import com.studypals.domain.studyManage.dto.GetCategoryRes;
import com.studypals.domain.studyManage.dto.UpdateCategoryReq;
import com.studypals.domain.studyManage.dto.mappers.CategoryMapper;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.worker.StudyCategoryReader;
import com.studypals.global.exceptions.errorCode.StudyErrorCode;
import com.studypals.global.exceptions.exception.StudyException;

/**
 * {@link StudyCategoryService} 에 대한 테스트코드입니다.
 *
 * @author jack8
 * @since 2025-04-12
 */
@ExtendWith(MockitoExtension.class)
class StudyCategoryServiceTest {

    @Mock
    private MemberReader memberReader;

    @Mock
    private StudyCategoryReader studyCategoryReader;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private Member mockMember;

    @Mock
    private StudyCategory mockStudyCategory;

    @InjectMocks
    private StudyCategoryServiceImpl studyCategoryService;

    @Test
    void createCategory_success() {
        // given
        Long userId = 1L;
        Long savedCategoryId = 2L;
        CreateCategoryReq req = new CreateCategoryReq("name", "#FFFFFF", 12, "description");

        given(memberReader.findRef(userId)).willReturn(mockMember);
        given(categoryMapper.toEntity(req, mockMember)).willReturn(mockStudyCategory);
        given(mockStudyCategory.getId()).willReturn(savedCategoryId); // 실제로는 save 후 넣어지지만, mock unit test 이므로...

        // when
        Long value = studyCategoryService.createCategory(userId, req);

        // then
        assertThat(value).isEqualTo(savedCategoryId);
    }

    @Test
    void createCategory_fail_whileSave() {
        // given
        Long userId = 1L;
        StudyErrorCode errorCode = StudyErrorCode.STUDY_CATEGORY_ADD_FAIL;
        CreateCategoryReq req = new CreateCategoryReq("name", "#FFFFFF", 12, "description");
        given(memberReader.findRef(userId)).willReturn(mockMember);
        given(categoryMapper.toEntity(req, mockMember)).willReturn(mockStudyCategory);
        given(mockStudyCategory.getId()).willThrow(new StudyException(errorCode));

        // when & then
        assertThatThrownBy(() -> studyCategoryService.createCategory(userId, req))
                .isInstanceOf(StudyException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }

    @Test
    void getUserCategory_success() {
        // given
        Long userId = 1L;
        GetCategoryRes res = new GetCategoryRes(1L, "category", "#FFFFFF", 12, "description");
        given(studyCategoryReader.findByMember(userId)).willReturn(List.of(mockStudyCategory));
        given(categoryMapper.toDto(mockStudyCategory)).willReturn(res);

        // when
        List<GetCategoryRes> value = studyCategoryService.getUserCategory(userId);

        // then
        assertThat(value).hasSize(1);
        assertThat(value.get(0)).isEqualTo(res);
    }

    @Test
    void getUserCategory_success_nothingToReturn() {
        // given
        Long userId = 1L;
        given(studyCategoryReader.findByMember(userId)).willReturn(List.of());

        // when
        List<GetCategoryRes> value = studyCategoryService.getUserCategory(userId);

        // then
        assertThat(value).hasSize(0);
        then(categoryMapper).shouldHaveNoInteractions();
    }

    @Test
    void getUserCategoryByDate_success_matchWednesday() {
        // given
        Long userId = 1L;
        LocalDate wednesday = LocalDate.of(2025, 4, 16); // 수요일
        int dayBit = 1 << 2; // 수요일 = 1 << 2

        StudyCategory cat1 = StudyCategory.builder().dayBelong(dayBit).build();
        StudyCategory cat2 =
                StudyCategory.builder().dayBelong(dayBit | (1 << 4)).build();

        GetCategoryRes res1 = new GetCategoryRes(1L, "category1", "#FFF", dayBit, "desc1");
        GetCategoryRes res2 = new GetCategoryRes(2L, "category2", "#000", dayBit | (1 << 4), "desc2");

        given(studyCategoryReader.findByMemberAndDay(userId, dayBit)).willReturn(List.of(cat1, cat2));
        given(categoryMapper.toDto(cat1)).willReturn(res1);
        given(categoryMapper.toDto(cat2)).willReturn(res2);

        // when
        List<GetCategoryRes> result = studyCategoryService.getUserCategoryByDate(userId, wednesday);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactlyInAnyOrder("category1", "category2");
    }

    @Test
    void getUserCategoryByDate_success_noMatch() {
        // given
        Long userId = 1L;
        LocalDate sunday = LocalDate.of(2025, 4, 13); // 일요일
        int dayBit = 1 << (sunday.getDayOfWeek().getValue() - 1);

        given(studyCategoryReader.findByMemberAndDay(userId, dayBit)).willReturn(List.of());

        // when
        List<GetCategoryRes> result = studyCategoryService.getUserCategoryByDate(userId, sunday);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void updateCategory_success() {
        // given
        Long userId = 1L;
        Long categoryId = 1L;
        UpdateCategoryReq req = new UpdateCategoryReq(categoryId, "new category", "#FFFFFF", 12, "new description");

        given(studyCategoryReader.findAndValidate(userId, categoryId)).willReturn(mockStudyCategory);
        given(mockStudyCategory.getId()).willReturn(categoryId);

        // when
        Long updatedCategoryId = studyCategoryService.updateCategory(userId, req);

        // then
        assertThat(updatedCategoryId).isEqualTo(categoryId);
        then(mockStudyCategory).should().updateCategory(req);
    }
}
