package com.studypals.domain.studyManage.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.worker.MemberReader;
import com.studypals.domain.studyManage.dto.CreateCategoryReq;
import com.studypals.domain.studyManage.dto.GetCategoryRes;
import com.studypals.domain.studyManage.dto.UpdateCategoryReq;
import com.studypals.domain.studyManage.dto.mappers.CategoryMapper;
import com.studypals.domain.studyManage.entity.PersonalStudyCategory;
import com.studypals.domain.studyManage.entity.StudyStatus;
import com.studypals.domain.studyManage.worker.StudyCategoryReader;
import com.studypals.domain.studyManage.worker.StudyCategoryWriter;
import com.studypals.domain.studyManage.worker.StudyStatusWorker;
import com.studypals.domain.studyManage.worker.StudyTimeWriter;
import com.studypals.global.exceptions.errorCode.StudyErrorCode;
import com.studypals.global.exceptions.exception.StudyException;

/**
 * PersonalStudyCategory 에 대한 service implement class 입니다.
 * <p>
 * 기본적인 CRUD 알고리즘이 포함되어 있습니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link StudyCategoryService} 의 구현 클래스입니다.
 *
 * <p><b>빈 관리:</b><br>
 * register as : StudyCategoryService
 * DI from : MemberRepository, PersonalStudyCategoryRepository
 *
 * @author jack8
 * @see StudyCategoryService
 * @since 2025-04-12
 */
@Service
@RequiredArgsConstructor
public class StudyCategoryServiceImpl implements StudyCategoryService {

    private final MemberReader memberReader;
    private final StudyCategoryWriter studyCategoryWriter;
    private final StudyCategoryReader studyCategoryReader;
    private final StudyStatusWorker studyStatusWorker;
    private final StudyTimeWriter studyTimeWriter;
    private final CategoryMapper categoryMapper;

    /*tested*/
    @Override
    @Transactional
    public Long createCategory(Long userId, CreateCategoryReq dto) {
        Member member = memberReader.getRef(userId);
        PersonalStudyCategory category = categoryMapper.toEntity(dto, member);

        studyCategoryWriter.save(category);

        return category.getId();
    }
    /*tested*/
    @Override
    @Transactional(readOnly = true)
    public List<GetCategoryRes> getUserCategory(Long userId) {

        return studyCategoryReader.findByMember(userId).stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    /*tested*/
    @Override
    @Transactional(readOnly = true)
    public List<GetCategoryRes> getUserCategoryByDate(Long userId, LocalDate date) {

        int dayBit = 1 << (date.getDayOfWeek().getValue() - 1);

        List<PersonalStudyCategory> categories = studyCategoryReader.getListByMemberAndDay(userId, dayBit);

        return categories.stream().map(categoryMapper::toDto).toList();
    }

    @Override
    @Transactional
    public Long updateCategory(Long userId, UpdateCategoryReq dto) {

        PersonalStudyCategory category = studyCategoryReader.getAndValidate(userId, dto.categoryId());
        category.updateCategory(dto);

        return category.getId();
    }

    @Override
    @Transactional
    public void deleteCategory(Long userId, Long categoryId) {

        PersonalStudyCategory category = studyCategoryReader.getAndValidate(userId, categoryId);
        Optional<StudyStatus> status = studyStatusWorker.find(userId);

        if (status.isPresent() && status.get().getId().equals(categoryId)) {
            throw new StudyException(
                    StudyErrorCode.STUDY_CATEGORY_DELETE_FAIL_PENDING_STUDY,
                    "[StudyCategoryServiceImpl#deleteCategory] try to delete pending study category");
        }

        studyTimeWriter.changeStudyTimeToRemoved(userId, categoryId);

        studyCategoryWriter.delete(category);
    }

    @Override
    @Transactional
    public void initCategory(Long userId) {

        studyCategoryWriter.deleteAll(userId);
    }
}
