package com.studypals.domain.studyManage.worker.categoryStrategy;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyType;
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
 * @since 2025-08-06
 */
@Component
public class RemovedCategoryStrategy implements CategoryStrategy {
    @Override
    public StudyType getType() {
        return StudyType.REMOVED;
    }

    @Override
    public boolean supports(StudyType type) {
        return type == getType();
    }

    @Override
    public void validateToCreate(Long userId, Long typeId) {
        throw new StudyException(
                StudyErrorCode.STUDY_CATEGORY_ADD_FAIL,
                "[RemovedCategoryStrategy#validateToCreate] removed category cannot add");
    }

    @Override
    public void validateToRead(Long userId, StudyCategory studyCategory) {
        if (!Objects.equals(userId, studyCategory.getTypeId())) {
            throw new StudyException(
                    StudyErrorCode.STUDY_CATEGORY_ACCESS_FAIL, "[RemovedCategoryStrategy#validateToRead] access fail");
        }
    }

    @Override
    public void validateToWrite(Long userId, StudyCategory studyCategory) {
        throw new StudyException(
                StudyErrorCode.STUDY_CATEGORY_ACCESS_FAIL,
                "[RemovedCategoryStrategy#validateToWrite] removed category cannot change");
    }

    @Override
    public Map<StudyType, List<Long>> getMapByUserId(Long userId) {
        return Map.of(getType(), List.of(userId));
    }
}
