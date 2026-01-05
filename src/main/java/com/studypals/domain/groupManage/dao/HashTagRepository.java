package com.studypals.domain.groupManage.dao;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.studypals.domain.groupManage.entity.HashTag;

/**
 * {@link HashTag} 에 대한 dao 클래스입니다.
 *
 * @author jack8
 * @see HashTag
 * @since 2025-12-23
 */
@Repository
public interface HashTagRepository extends JpaRepository<HashTag, Long> {

    /**
     * tag 에 대해 객체를 반환합니다.
     * @param tag 검색할 태그(정확히 일치)
     * @return Optional Hash tag
     */
    Optional<HashTag> findByTag(String tag);

    /**
     * tag 자동완성 시 사용할 메서드. prefix 에 대해 cnt 값 만큼의 자주 사용되는 데이터를 반환합니다.
     *
     * @param value 검색할 인자(접두사 / 순서대로)
     * @param pageable 반환 개수 지정
     * @return cnt 개수 만큼의, 사용 빈도가 높은 데이터
     */
    @Query(
            """
    SELECT t.tag
    FROM HashTag t
    WHERE t.tag LIKE CONCAT('%', :value, '%')
    ORDER BY t.usedCount DESC
""")
    List<String> search(@Param("value") String value, Pageable pageable);

    /**
     * usedCount 값을 원자적으로 증가시키는 메서드입니다. 해당 메서드가 실행 되면
     * 이미 해당 태그를 사용했다는 의미이므로 deletedAt 을 초기화합니다.
     * @param tag 증가시킬 태그
     * @return 변경된 row 수
     */
    @Modifying
    @Query(
            """
        UPDATE HashTag t
          SET t.usedCount = t.usedCount + 1,
            t.deletedAt = null
        WHERE t.tag = :tag
    """)
    Integer increaseUsedCount(String tag);

    /**
     * usedCount 값을 원자적으로 증가시키는 메서드입니다. 단, 여러 tags 들에 대해 연산을 수행합니다.
     * @param tags 증가시킬 태그들
     * @return 변경된 row 수
     */
    @Modifying
    @Query(
            """
        UPDATE HashTag t
          SET t.usedCount = t.usedCount + 1,
            t.deletedAt = null
        WHERE t.tag in (:tags)
    """)
    void increaseUsedCountBulk(@Param("tags") Collection<String> tags);

    /**
     * usedCount 값을 원자적으로 감소시키는 메서드입니다. 만약 0이 되면,
     * 그때부터 deletedAt 을 현재 시간으로 설정합니다.  n 일 이후 자동 삭제됩니다(최적화, 배치 서버 분리)
     * @param tag 감소시킬 태그
     * @return 변경된 row 수
     */
    @Modifying
    @Query(
            """
    UPDATE HashTag t
       SET t.usedCount = t.usedCount - 1,
           t.deletedAt = CASE
               WHEN (t.usedCount - 1) = 0 THEN CURRENT_TIMESTAMP
               ELSE t.deletedAt
           END
    WHERE t.tag = :tag
       AND t.usedCount > 0
""")
    Long decreaseUsedCount(String tag);

    /**
     * tag 리스트에 대한 전체 조회 반환 메서드입니다.
     * @param tags 문자열 리스트
     * @return 파라미터에 대해 정확히 일치하는 hash tag 엔티티 리스트
     */
    List<HashTag> findAllByTagIn(Collection<String> tags);
}
