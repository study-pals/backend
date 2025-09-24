package com.studypals.domain.studyManage.service;

import java.util.List;
import java.util.Map;

import com.studypals.domain.studyManage.dto.UpdateCategoryDto;
import com.studypals.domain.studyManage.entity.DateType;
import com.studypals.global.utils.ImageUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.studyManage.dto.CreateCategoryDto;
import com.studypals.domain.studyManage.dto.GetCategoryRes;
import com.studypals.domain.studyManage.dto.UpdateCategoryReq;
import com.studypals.domain.studyManage.dto.mappers.CategoryMapper;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyType;
import com.studypals.domain.studyManage.worker.StudyCategoryReader;
import com.studypals.domain.studyManage.worker.StudyCategoryWriter;
import com.studypals.domain.studyManage.worker.StudyStatusWorker;
import com.studypals.domain.studyManage.worker.categoryStrategy.CategoryStrategy;
import com.studypals.domain.studyManage.worker.categoryStrategy.CategoryStrategyFactory;
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
 *
 * @author jack8
 * @see StudyCategoryService
 * @since 2025-04-12
 */
@Service
@RequiredArgsConstructor
public class StudyCategoryServiceImpl implements StudyCategoryService {

    private final StudyCategoryWriter studyCategoryWriter;
    private final StudyCategoryReader studyCategoryReader;
    private final StudyStatusWorker studyStatusWorker;
    private final CategoryMapper categoryMapper;

    private final CategoryStrategyFactory categoryStrategyFactory;

    /*tested*/
    @Override
    @Transactional
    public Long createCategory(Long userId, CreateCategoryDto dto) {
        // 만들고자 하는 카테고리에 맞는 전략 객체 선택 - 생성할 권한이 있는지 검사
        CategoryStrategy strategy = categoryStrategyFactory.resolve(dto.studyType());
        strategy.validateToCreate(userId, dto.typeId());

        StudyCategory category = categoryMapper.toEntity(dto);

        studyCategoryWriter.save(category);

        return category.getId();
    }

    /*tested*/
    @Override
    @Transactional(readOnly = true)
    public List<GetCategoryRes> getAllUserCategories(Long userId) {
        // 특정 유저에 대해, 해당 유저가 표시 가능한 모든 카테고리 타입 - 타입 아이디 리스트
        Map<StudyType, List<Long>> typeMap = categoryStrategyFactory.getTypeMap(userId);

        // 위 값을 바탕으로 검색
        return studyCategoryReader.findByTypesAndTypeIds(typeMap).stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetCategoryRes> getGroupCategories(Long groupId) {
        return studyCategoryReader.findByStudyTypeAndTypeId(StudyType.GROUP, groupId).stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public Long updateCategory(Long userId, UpdateCategoryReq req) {
        ifStudyNotWrite(userId);

        StudyCategory category = studyCategoryReader.getById(req.categoryId());

        CategoryStrategy strategy = categoryStrategyFactory.resolve(category.getStudyType());
        strategy.validateToWrite(userId, category);

        UpdateCategoryDto dto = UpdateCategoryDto.builder()
                .name(req.name() == null ? category.getName() : req.name())
                .color(req.color() == null ? ImageUtils.randomHexColor() : req.color())
                .dateType(req.dateType() == null ? DateType.DAILY : req.dateType())
                .dayBelong(req.dayBelong() == null ? 127 : req.dayBelong())
                .description(req.description() == null ? "no content" : req.description())
                .goal(req.goal())
                .build();

        studyCategoryWriter.update(category, dto);
        studyCategoryWriter.save(category);

        return category.getId();
    }

    @Override
    @Transactional
    public void deleteCategory(Long userId, Long categoryId) {
        ifStudyNotWrite(userId);

        StudyCategory category = studyCategoryReader.getById(categoryId);

        CategoryStrategy strategy = categoryStrategyFactory.resolve(category.getStudyType());
        strategy.validateToWrite(userId, category);

        studyCategoryWriter.remove(category);
    }

    // 공부 중 카테고리 정보 갱신/삭제 불가능 검증
    private void ifStudyNotWrite(Long userId) {
        if (studyStatusWorker.isStudying(userId)) {
            throw new StudyException(
                    StudyErrorCode.STUDY_CATEGORY_DELETE_FAIL_PENDING_STUDY,
                    "[StudyCategoryServiceImpl#ifStudyNotWrite] change category while studying");
        }
    }
}
