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
 * {@link CategoryStrategy} 에 대한 구현 클래스이자, {@link StudyType} 이 {@code StudyType.REMOVED} 인 경우에 사용됩니다.
 *
 * <p><b>상속 정보:</b><br>
 * CategoryStrategy 에 대한 구현 클래스
 *
 * <p><b>빈 관리:</b><br>
 * Component 이며 {@link CategoryStrategyFactory} 에 List 로 주입됩니다.
 *
 * @author jack8
 * @see CategoryStrategyFactory
 * @see CategoryStrategy
 * @since 2025-08-04
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

    /**
     * 애초에 REMOVED 는 생성이 불가능합니다. 무조건 예외를 던집니다.
     * @throws StudyException StudyErrorCode.STUDY_CATEGORY_ADD_FAIL
     * @param userId 생성 요청을 보낸 유저
     * @param typeId 만들 카테고리의 typeId
     */
    @Override
    public void validateToCreate(Long userId, Long typeId) {
        throw new StudyException(
                StudyErrorCode.STUDY_CATEGORY_ADD_FAIL,
                "[RemovedCategoryStrategy#validateToCreate] removed category cannot add");
    }

    /**
     * REMOVED 는 PERSONAL 타입이 삭제된 경우의 타입입니다. typeId 를 단순 비교합니다.
     * @param userId 읽을 사용자 아이디
     * @param studyCategory 읽을 엔티티
     */
    @Override
    public void validateToRead(Long userId, StudyCategory studyCategory) {
        if (!Objects.equals(userId, studyCategory.getTypeId())) {
            throw new StudyException(
                    StudyErrorCode.STUDY_CATEGORY_ACCESS_FAIL, "[RemovedCategoryStrategy#validateToRead] access fail");
        }
    }

    /**
     * REMOVED 는 한번 변경된 이후 수정이 불가능합니다. 무조건 예외를 던집니다.
     * @throws StudyException StudyErrorCode.STUDY_CATEGORY_ACCESS_FAIL
     * @param userId 갱신 요청한 유저 아이디
     * @param studyCategory 갱신할 카테고리 엔티티
     */
    @Override
    public void validateToWrite(Long userId, StudyCategory studyCategory) {
        throw new StudyException(
                StudyErrorCode.STUDY_CATEGORY_ACCESS_FAIL,
                "[RemovedCategoryStrategy#validateToWrite] removed category cannot change");
    }

    /**
     * REMOVED 는 PERSONAL 타입이 삭제된 경우입니다. 단순 userId 로 엮어서 반환합니다.
     * @param userId 검색할 유저 아이디
     * @return StudyType.REMOVED, List.of(userId)
     */
    @Override
    public Map<StudyType, List<Long>> getMapByUserId(Long userId) {
        return Map.of(getType(), List.of(userId));
    }
}
