package com.studypals.domain.memberManage.dao;

import com.studypals.domain.memberManage.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * 토큰을 저장하는 redis repository
 * <p>
 * redis 기반의 CRUDRepository 입니다. {@link RefreshToken} 을 관리합니다.
 *
 * <p><b>상속 정보:</b><br>
 * CRUDRepository의 확장 인터페이스입니다.
 *
 * @author jack8
 * @see RefreshToken
 * @since 2025-04-04
 */
@Repository
public interface RefreshTokenRedisRepository extends CrudRepository<RefreshToken, Long> {

}
