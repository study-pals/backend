package com.studypals.domain.studyManage.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.worker.MemberReader;
import com.studypals.domain.studyManage.dto.CreateCategoryReq;
import com.studypals.domain.studyManage.dto.GetCategoryRes;
import com.studypals.domain.studyManage.dto.UpdateCategoryReq;
import com.studypals.domain.studyManage.dto.mappers.CategoryMapper;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.worker.StudyCategoryReader;
import com.studypals.domain.studyManage.worker.StudyCategoryWriter;

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

    private final MemberReader memberReader;
    private final StudyCategoryWriter studyCategoryWriter;
    private final StudyCategoryReader studyCategoryReader;
    private final CategoryMapper categoryMapper;

    /*tested*/
    @Override
    @Transactional
    public Long createCategory(Long userId, CreateCategoryReq dto) {
        Member member = memberReader.getRef(userId);
        StudyCategory category = categoryMapper.toEntity(dto, member);

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

        List<StudyCategory> categories = studyCategoryReader.getListByMemberAndDay(userId, dayBit);

        return categories.stream().map(categoryMapper::toDto).toList();
    }

    @Override
    @Transactional
    public Long updateCategory(Long userId, UpdateCategoryReq dto) {

        StudyCategory category = studyCategoryReader.getAndValidate(userId, dto.categoryId());
        category.updateCategory(dto);

        return category.getId();
    }

    @Override
    @Transactional
    public void deleteCategory(Long userId, Long categoryId) {

        StudyCategory category = studyCategoryReader.getAndValidate(userId, categoryId);

        studyCategoryWriter.delete(category);
    }

    @Override
    @Transactional
    public void initCategory(Long userId) {

        studyCategoryWriter.deleteAll(userId);
    }
}
