package com.studypals.domain.groupManage.service;

import java.util.List;

import com.studypals.domain.groupManage.dto.CreateGroupReq;
import com.studypals.domain.groupManage.dto.GetGroupTagRes;

/**
 * GroupService 의 인터페이스입니다. 메서드를 정의합니다.
 *
 * <p>
 *
 * <p><b>상속 정보:</b><br>
 * GroupServiceImpl의 부모 인터페이스입니다.
 *
 * @author s0o0bn
 * @see GroupServiceImpl
 * @since 2025-04-12
 */
public interface GroupService {

    /**
     * 그룹 태그를 조회합니다.
     *
     * @return 사전에 저장된 그룹 태그 리스트
     */
    List<GetGroupTagRes> getGroupTags();

    /**
     * 그룹을 생성하고 생성한 사용자에 그룹장 권한을 부여합니다.
     *
     * @param userId 그룹을 생성할 사용자
     * @param dto 그룹 생성 시 필요한 데이터
     * @return 생성된 그룹 ID
     * @throws com.studypals.global.exceptions.exception.GroupException
     */
    Long createGroup(Long userId, CreateGroupReq dto);
}
