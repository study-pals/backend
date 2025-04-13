package com.studypals.domain.studyManage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.memberManage.dao.MemberRepository;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.dao.StudyCategoryRepository;
import com.studypals.domain.studyManage.dto.CreateCategoryReq;
import com.studypals.domain.studyManage.dto.GetCategoryRes;
import com.studypals.domain.studyManage.dto.UpdateCategoryReq;
import com.studypals.domain.studyManage.dto.mappers.CategoryMapper;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.global.exceptions.errorCode.StudyErrorCode;
import com.studypals.global.exceptions.exception.StudyException;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ExampleClass(String example)}  <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author jack8
 * @see
 * @since 2025-04-12
 */
@ExtendWith(MockitoExtension.class)
class StudyCategoryServiceTest {

    @Mock
    private StudyCategoryRepository studyCategoryRepository;

    @Mock
    private MemberRepository memberRepository;

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

        given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember));
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
        given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember));
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
        given(studyCategoryRepository.findByMemberId(userId)).willReturn(List.of(mockStudyCategory));
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
        given(studyCategoryRepository.findByMemberId(userId)).willReturn(List.of());

        // when
        List<GetCategoryRes> value = studyCategoryService.getUserCategory(userId);

        // then
        assertThat(value).hasSize(0);
        then(categoryMapper).shouldHaveNoInteractions();
    }

    @Test
    void updateCategory_success() {
        // given
        Long userId = 1L;
        Long categoryId = 1L;
        UpdateCategoryReq req = new UpdateCategoryReq(categoryId, "new category", "#FFFFFF", 12, "new description");

        given(studyCategoryRepository.findById(categoryId)).willReturn(Optional.of(mockStudyCategory));
        given(mockStudyCategory.getMember()).willReturn(mockMember);
        given(mockMember.getId()).willReturn(1L);

        // when
        studyCategoryService.updateCategory(userId, req);

        // then
        then(mockStudyCategory).should().updateCategory(req);
    }
}
