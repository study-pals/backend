package com.studypals.domain.studyManage.dao;

import org.springframework.stereotype.Repository;

import com.studypals.domain.studyManage.entity.DailyStudySummary;
import com.studypals.global.dao.ReadOnlyRepository;

/**
 * {@link DailyStudySummary} 에 대한 JPA readonly DAO 인터페이스입니다.
 *
 * @author jack8
 * @see DailyStudySummary
 * @since 2025-04-17
 */
@Repository
public interface DailyStudySummaryRepository extends ReadOnlyRepository<DailyStudySummary, Long> {}
