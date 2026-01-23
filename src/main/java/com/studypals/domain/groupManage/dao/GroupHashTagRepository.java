package com.studypals.domain.groupManage.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.studypals.domain.groupManage.entity.GroupHashTag;

/**
 * {@link GroupHashTag} 에 대한 dao 클래스입니다.
 *
 * @author jack8
 * @see GroupHashTag
 * @since 2025-12-23
 */
@Repository
public interface GroupHashTagRepository extends JpaRepository<GroupHashTag, Long> {

    /**
     * 일반적인 {@code findAllByGroupId} 와 결과가 동일하나, hashTag 에 대한 fetch join 을 통한
     * N+1 문제를 방지하였습니다.
     * @param groupId 검색하고자 하는 그룹의 아이디
     * @return hash tag 가 fetch join 된 groupHashTag 리스트
     */
    @Query(
            """
        SELECT gt
        FROM GroupHashTag gt
        JOIN FETCH gt.hashTag
        WHERE gt.group.id = :groupId
    """)
    List<GroupHashTag> findAllByGroupIdWithTag(@Param("groupId") Long groupId);

    List<GroupHashTag> findAllByGroupId(Long groupId);

    /**
     * GroupHashTag 에서 Group 에 대해, id 를 제외한 다른 데이터를 조회 시 N + 1 문제를 야기할 수 있습니다. 따라서 해당 메서드를
     * 사용하기 앞서 group 에 대해 ID 외 다른 칼럼을 조회하는지에 대한 확인이 필요합니다.
     * @param groupIds 검색하고자 하는 그룹 ID 리스트
     * @return groupHashTag 리스트(단 group 은 lazy loading)
     */
    List<GroupHashTag> findAllByGroupIdIn(List<Long> groupIds);
}
