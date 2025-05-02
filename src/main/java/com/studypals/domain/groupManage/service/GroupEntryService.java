package com.studypals.domain.groupManage.service;

import com.studypals.domain.groupManage.dto.GroupEntryCodeRes;
import com.studypals.domain.groupManage.dto.GroupEntryReq;
import com.studypals.domain.groupManage.dto.GroupSummaryRes;

/**
 * GroupEntryService 의 인터페이스입니다. 메서드를 정의합니다.
 *
 * <p>
 *
 * <p><b>상속 정보:</b><br>
 * GroupEntryServiceImpl의 부모 인터페이스입니다.
 *
 * @author s0o0bn
 * @see GroupEntryServiceImpl
 * @since 2025-04-25
 */
public interface GroupEntryService {

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
     * @throws com.studypals.global.exceptions.exception.GroupException
     */
    GroupSummaryRes getGroupSummary(String entryCode);

    /**
     * 공개 그룹에 승인 없이 바로 가입합니다.
     *
     * @param userId 가입할 사용자 ID
     * @param entryInfo 가입할 그룹 정보 {@link GroupEntryReq}
     * @return {@link com.studypals.domain.groupManage.entity.GroupMember} ID
     */
    Long joinGroup(Long userId, GroupEntryReq entryInfo);

    /**
     * 비공개 그룹에 가입 요청을 보냅니다.
     *
     * @param userId 요청할 사용자 ID
     * @param entryInfo 요청할 그룹 정보 {@link GroupEntryReq}
     * @return {@link com.studypals.domain.groupManage.entity.GroupEntryRequest} ID
     */
    Long requestParticipant(Long userId, GroupEntryReq entryInfo);
}
