package com.studypals.domain.studyManage.worker.categoryStrategy;

import java.util.List;
import java.util.Map;

import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyType;

/**
 * {@link StudyType} 에 따른 읽기/쓰기 권한 및 검증, 검색 전략 등을 정의합니다. {@link CategoryStrategyFactory} 에 의해 구현체를 가져와
 * 사용합니다.
 * <p>
 *
 * <p><b>상속 정보:</b><br>
 * 모든 전략 패턴 구현체의 부모 인터페이스
 *
 * @author jack8
 * @see CategoryStrategyFactory
 * @see SimpCategoryStrategy
 * @since 2025-08-04
 */
public interface CategoryStrategy {

    /**
     * 해당 구현체가 어떤 StudyType 을 지원하는지를 반환합니다.
     * @return StudyType 지원하는 타입
     */
    StudyType getType();

    /**
     * 해당 구현체가 입력받은 매개변수에 대해 지원하는지 여부를 반환합니다.
     * @param type 알고자 하는 타입
     * @return boolean - 지원하는지 여부
     */
    boolean supports(StudyType type);

    /**
     * 해당 카테고리를 생성할 권한이 있는지를 검증합니다. 요청을 보낸 유저와, 해당 유저가 생성하고자 할 카테고리와 연관된 테이블의 id 를 받습니다.
     * 검증 실패 시 예외를 반환합니다.
     * @param userId 생성 요청을 보낸 유저
     * @param typeId 만들 카테고리의 typeId
     */
    void validateToCreate(Long userId, Long typeId);

    /**
     * 해당 카테고리를 읽을 권한이 있는지 검증합니다. 읽을 사용자 아이디와 읽고자 할 엔티티를 받습니다. 검증 실패 시 예외를 반환합니다.
     * @param userId 읽을 사용자 아이디
     * @param studyCategory 읽을 엔티티
     */
    void validateToRead(Long userId, StudyCategory studyCategory);

    /**
     * 해당 카테고리를 갱신할 권한이 있는지 검증합니다. 갱신 요청한 사용자 아이디와 갱신하고자 할 카테고리 엔티티를 받습니다.
     * 검증 실패 시 예외를 반환합니다.
     * @param userId 갱신 요청한 유저 아이디
     * @param studyCategory 갱신할 카테고리 엔티티
     */
    void validateToWrite(Long userId, StudyCategory studyCategory);

    /**
     * 특정 유저에 대해 해당 유저가 소유한(혹은 공부 가능한) 카테고리를 검색하고, 이를 StudyType - typeId list 의 맵으로 반환합니다.
     * 해당 메서드를 통해 유저의 카테고리 정보를 전부 불러올 수 있습니다.
     *
     * @param userId 검색할 유저 아이디
     * @return 타입과 그에 따른 typeId 정보
     */
    Map<StudyType, List<Long>> getMapByUserId(Long userId);
}
