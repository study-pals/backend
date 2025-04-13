package com.studypals.domain.groupManage.dao;

import java.util.List;

import org.springframework.data.repository.Repository;

import com.studypals.domain.groupManage.entity.GroupTag;

/**
 * {@link GroupTag} 엔티티에 대한 dao 클래스입니다.
 * 해당 dao는 읽기 전용입니다.
 *
 * <p>JPA 기반의 repository
 *
 * <p><b>상속 정보:</b><br>
 * {@code Repository<GroupTag, String>}
 *
 * @author s0o0bn
 * @see GroupTag
 * @since 2025-04-13
 */
public interface GroupTagRepository extends Repository<GroupTag, String> {

    List<GroupTag> findAll();

    boolean existsById(String name);
}
