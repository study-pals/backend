package com.studypals.domain.groupManage.dao.groupEntryRepository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.studypals.domain.groupManage.dao.GroupEntryCodeRedisRepositoryCustom;
import com.studypals.domain.groupManage.entity.GroupEntryCode;

/**
 * 그룹 초대 코드를 저장하는 redis repository
 *
 * <p>redis 기반의 CRUDRepository 입니다. {@link GroupEntryCode} 을 관리합니다.
 *
 * <p><b>상속 정보:</b><br>
 * CRUDRepository의 확장 인터페이스입니다.
 *
 * @author s0o0bn
 * @see GroupEntryCode
 * @since 2025-04-15
 */
@Repository
public interface GroupEntryCodeRedisRepository
        extends CrudRepository<GroupEntryCode, String>, GroupEntryCodeRedisRepositoryCustom {}
