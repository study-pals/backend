package com.studypals.domain.groupManage.service;

import java.util.List;

import com.studypals.domain.groupManage.dto.CreateGroupReq;
import com.studypals.domain.groupManage.dto.GetGroupTagRes;
import com.studypals.domain.groupManage.dto.GroupEntryCodeRes;
import com.studypals.domain.groupManage.dto.GroupSummaryRes;

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

    /**
     * 그룹 초대 코드를 생성한다.
     * 코드 생성은 그룹장만 가능하다.
     *
     * @param userId 초대 코드를 생성한 사용자 ID
     * @param groupId 코드를 생성할 그룹 ID
     * @return 그룹 ID와 해당 그룹의 초대 코드
     */
    GroupEntryCodeRes generateEntryCode(Long userId, Long groupId);

    /**
     * 초대 코드로 그룹 대표 정보를 조회합니다.
     * 그룹장 포함 일부 멤버들의 프로필 이미지와 함께 조회합니다.
     *
     * @param entryCode 그룹 초대 코드
     * @return 그룹 대표 정보
     */
    GroupSummaryRes getGroupSummary(String entryCode);
}
