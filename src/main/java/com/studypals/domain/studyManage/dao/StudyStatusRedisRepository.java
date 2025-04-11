package com.studypals.domain.studyManage.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.studypals.domain.studyManage.entity.StudyStatus;

/**
 * {@link StudyStatus} 에 대한 Redis DAO 클래스입니다.
 * <p>
 * 유저의 공부 상태, 공부 시간 등에 대한 데이터를 저장합니다.
 *
 * <p><b>상속 정보:</b><br>
 * CRUDRepository의 확장 인터페이스입니다.
 *
 * @author jack8
 * @since 2025-04-10
 */
@Repository
public interface StudyStatusRedisRepository extends CrudRepository<StudyStatus, Long> {}
