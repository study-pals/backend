package com.studypals.domain.studyManage.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.memberManage.dao.MemberRepository;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.dao.StudyCategoryRepository;
import com.studypals.domain.studyManage.dto.CreateCategoryReq;
import com.studypals.domain.studyManage.dto.DeleteCategoryReq;
import com.studypals.domain.studyManage.dto.UpdateCategoryReq;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.global.exceptions.errorCode.AuthErrorCode;
import com.studypals.global.exceptions.errorCode.StudyErrorCode;
import com.studypals.global.exceptions.exception.AuthException;
import com.studypals.global.exceptions.exception.StudyException;

/**
 * StudyCategory 에 대한 service implement class 입니다.
 * <p>
 * 기본적인 CRUD 알고리즘이 포함되어 있습니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link StudyCategoryService} 의 구현 클래스입니다.
 *
 * <p><b>빈 관리:</b><br>
 * register as : StudyCategoryService
 * DI from : MemberRepository, StudyCategoryRepository
 *
 * @author jack8
 * @see StudyCategoryService
 * @since 2025-04-12
 */
@Service
@RequiredArgsConstructor
public class StudyCategoryServiceImpl implements StudyCategoryService {

    private final MemberRepository memberRepository;
    private final StudyCategoryRepository studyCategoryRepository;

    @Override
    @Transactional
    public Long createCategory(CreateCategoryReq dto) {
        Long userId = dto.userId();
        Member member = findMember(userId);
        StudyCategory category = dto.toEntity(member);

        studyCategoryRepository.save(category);

        return category.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudyCategory> getUserCategory(Long userId) {

        return studyCategoryRepository.findByMemberId(userId);
    }

    @Override
    @Transactional
    public void updateCategory(UpdateCategoryReq dto) {

        StudyCategory category = findCategory(dto.categoryId());
        if (!category.getMember().getId().equals(dto.userId())) {
            throw new StudyException(StudyErrorCode.STUDY_CATEGORY_UPDATE_FAIL, "owner of category deos not match");
        }
        category.updateCategory(dto);
    }

    @Override
    @Transactional
    public void deleteCategories(DeleteCategoryReq dto) {

        Long userId = dto.userId();
        for (Long categoryId : dto.categoryIds()) {
            StudyCategory category = findCategory(categoryId);

            if (!category.getMember().getId().equals(userId)) {
                throw new StudyException(StudyErrorCode.STUDY_CATEGORY_DELETE_FAIL, "owner of category deos not match");
            }

            studyCategoryRepository.deleteById(categoryId);
        }
    }

    /**
     * member 를 찾는 private method. 실패 시 예외 처리의 공통화를 위해 분리하였다.
     *
     * @param id 찾고자 하는 member 의 id
     * @return Member
     * @throws AuthException {@code AuthErrorCode.USER_NOT_FOUND, "In StudyCategoryServiceImpl} 포함
     */
    private Member findMember(Long id) {
        return memberRepository
                .findById(id)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND, "In StudyCategoryServiceImpl"));
    }

    /**
     * study category 를 찾는 private method. 실패 시 예외 처리의 공통화를 위해 분리하였다.
     *
     * @param id 찾고자 하는 category 의 id
     * @return Category
     * @throws StudyException {@code StudyErrorCode.STUDY_CATEGORY_NOT_FOUND, "In StudyCategoryServiceImpl} 포함
     */
    private StudyCategory findCategory(Long id) {
        return studyCategoryRepository
                .findById(id)
                .orElseThrow(() ->
                        new StudyException(StudyErrorCode.STUDY_CATEGORY_NOT_FOUND, "In StudyCategoryServiceImpl"));
    }
}
