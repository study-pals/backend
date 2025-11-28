package com.studypals.domain.studyManage.worker.categoryStrategy;

import java.util.Objects;

import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyType;
import com.studypals.global.exceptions.errorCode.StudyErrorCode;
import com.studypals.global.exceptions.exception.StudyException;

/**
 * {@link CategoryStrategy} 에 대한 추상 클래스. 여러 공통되는 부분에 대한 메서드를 정의해놓았습니다.
 * <p>
 * 특수한 경우를 제외한, CategoryStrategy의 일부 구현 클래스의 공통 메서드를 정의.
 *
 *
 * <p><b>상속 정보:</b><br>
 * {@link CategoryStrategy} 에 대한 추상 클래스. 일부 메서드 구현
 *
 *
 * @author jack8
 * @see CategoryStrategy
 * @since 2025-08-04
 */
public abstract class SimpCategoryStrategy implements CategoryStrategy {

    @Override
    public boolean supports(StudyType type) {
        return type == getType();
    }

    /**
     * 단순히 userId 와 typeId 일치 여부를 검사합니다. PERSONAL 등의 경우, 그 typeId 가 userId 입니다.
     * @param userId 생성 요청을 보낸 유저
     * @param typeId 만들 카테고리의 typeId
     */
    @Override
    public void validateToCreate(Long userId, Long typeId) {
        if (!Objects.equals(userId, typeId))
            throw new StudyException(
                    StudyErrorCode.STUDY_CATEGORY_ADD_FAIL,
                    "[SimpCategoryStrategy#validateToCreate] cannot create category with other user id");
    }

    /**
     * 카테고리의 typeId 와 userId 를 비교합니다.
     * @param userId 읽을 사용자 아이디
     * @param studyCategory 읽을 엔티티
     */
    @Override
    public void validateToRead(Long userId, StudyCategory studyCategory) {
        validateInternal(userId, studyCategory);
    }

    /**
     * 카테고리의 typeId 와 userId 를 비교합니다.
     * @param userId 갱신 요청한 유저 아이디
     * @param studyCategory 갱신할 카테고리 엔티티
     */
    @Override
    public void validateToWrite(Long userId, StudyCategory studyCategory) {
        validateInternal(userId, studyCategory);
    }

    private void validateInternal(Long userId, StudyCategory studyCategory) {
        if (!Objects.equals(userId, studyCategory.getTypeId())) {
            throw new StudyException(
                    StudyErrorCode.STUDY_CATEGORY_ACCESS_FAIL, "[SimpCategoryStrategy#validateInternal] access fail");
        }
    }
}
