package com.studypals.domain.studyManage.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

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
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public Long createCategory(Long userId, CreateCategoryReq dto) {
        Member member = memberRepository.getReferenceById(userId);
        StudyCategory category = categoryMapper.toEntity(dto, member);

        try {
            studyCategoryRepository.save(category);
        } catch (Exception e) {
            throw new StudyException(StudyErrorCode.STUDY_CATEGORY_ADD_FAIL);
        }

        return category.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetCategoryRes> getUserCategory(Long userId) {

        return studyCategoryRepository.findByMemberId(userId).stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetCategoryRes> getUserCategoryByDate(Long userId, LocalDate date) {

        int dayBit = 1 << (date.getDayOfWeek().getValue() - 1);

        return studyCategoryRepository.findByMemberId(userId).stream()
                .filter(category -> (category.getDayBelong() & dayBit) != 0)
                .map(categoryMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public Long updateCategory(Long userId, UpdateCategoryReq dto) {

        StudyCategory category = findCategory(dto.categoryId());
        if (!category.isOwner(userId)) {
            throw new StudyException(StudyErrorCode.STUDY_CATEGORY_UPDATE_FAIL, "owner of category deos not match");
        }
        category.updateCategory(dto);

        return category.getId();
    }

    @Override
    @Transactional
    public void deleteCategory(Long userId, Long categoryId) {

        StudyCategory category = findCategory(categoryId);

        if (!category.isOwner(userId)) {
            throw new StudyException(StudyErrorCode.STUDY_CATEGORY_DELETE_FAIL, "owner of category does not match");
        }

        studyCategoryRepository.deleteById(categoryId);
    }

    @Override
    @Transactional
    public void initCategory(Long userId) {

        studyCategoryRepository.deleteByMemberId(userId);
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
