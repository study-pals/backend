package com.studypals.domain.studyManage.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.memberManage.worker.MemberReader;
import com.studypals.domain.studyManage.dto.CreateCategoryDto;
import com.studypals.domain.studyManage.dto.GetCategoryRes;
import com.studypals.domain.studyManage.dto.UpdateCategoryReq;
import com.studypals.domain.studyManage.dto.mappers.CategoryMapper;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyStatus;
import com.studypals.domain.studyManage.worker.StudyCategoryReader;
import com.studypals.domain.studyManage.worker.StudyCategoryWriter;
import com.studypals.domain.studyManage.worker.StudyStatusWorker;
import com.studypals.domain.studyManage.worker.StudyTimeWriter;
import com.studypals.domain.studyManage.worker.validateStrategy.ValidateStrategy;
import com.studypals.domain.studyManage.worker.validateStrategy.ValidateStrategyFactory;
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

    private final ValidateStrategyFactory validateStrategyFactory;

    /*tested*/
    @Override
    @Transactional
    public Long createCategory(Long userId, CreateCategoryDto dto) {
        ValidateStrategy strategy = validateStrategyFactory.resolve(dto.studyType());
        strategy.validateToCreate(userId, dto.typeId());

        StudyCategory category = categoryMapper.toEntity(dto);

        studyCategoryWriter.save(category);

        return category.getId();
    }

    /*tested*/
    @Override
    @Transactional(readOnly = true)
    public List<GetCategoryRes> getUserCategory(Long userId) {

        return studyCategoryReader.findByMemberId(userId).stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public Long updateCategory(Long userId, UpdateCategoryReq dto) {
        StudyCategory category = studyCategoryReader.findById(dto.categoryId());

        ValidateStrategy strategy = validateStrategyFactory.resolve(category.getStudyType());
        strategy.validateToWrite(userId, category);

        studyCategoryWriter
                .update(category)
                .name(dto.name())
                .color(dto.color())
                .goal(dto.goal())
                .dateType(dto.dateType())
                .dayBelong(dto.dayBelong())
                .description(dto.description())
                .build();

        return category.getId();
    }

    @Override
    @Transactional
    public void deleteCategory(Long userId, Long categoryId) {

        StudyCategory category = studyCategoryReader.getAndValidate(userId, categoryId);

        ValidateStrategy strategy = validateStrategyFactory.resolve(category.getStudyType());
        strategy.validateToWrite(userId, category);

        Optional<StudyStatus> status = studyStatusWorker.findAndDelete(userId);

        if (status.isPresent() && status.get().getId().equals(categoryId)) {
            throw new StudyException(
                    StudyErrorCode.STUDY_CATEGORY_DELETE_FAIL_PENDING_STUDY,
                    "[StudyCategoryServiceImpl#deleteCategory] try to delete pending study category");
        }

        studyTimeWriter.changeStudyTimeToRemoved(userId, categoryId);

        studyCategoryWriter.delete(category);
    }
}
