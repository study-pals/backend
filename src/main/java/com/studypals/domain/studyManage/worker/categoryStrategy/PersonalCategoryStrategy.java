package com.studypals.domain.studyManage.worker.categoryStrategy;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.studypals.domain.studyManage.entity.StudyType;

/**
 * {@link SimpCategoryStrategy} 에 대한 구현 클래스이자, {@link StudyType} 이 {@code StudyType.PERSONAL} 인 경우에 사용됩니다.
 *
 * <p><b>상속 정보:</b><br>
 * SimpCategoryStrategy 에 대한 구현 클래스
 *
 * <p><b>빈 관리:</b><br>
 * Component 이며 {@link CategoryStrategyFactory} 에 List 로 주입됩니다.
 *
 * @author jack8
 * @see CategoryStrategyFactory
 * @see SimpCategoryStrategy
 * @see CategoryStrategy
 * @since 2025-08-04
 */
@Component
public class PersonalCategoryStrategy extends SimpCategoryStrategy {
    @Override
    public StudyType getType() {
        return StudyType.PERSONAL;
    }

    /**
     * @param userId 검색할 유저 아이디
     * @return StudyType.PERSONAL, List.of(usreId)
     */
    @Override
    public Map<StudyType, List<Long>> getMapByUserId(Long userId) {
        return Map.of(getType(), List.of(userId));
    }
}
