package com.studypals.domain.groupManage.service;

import com.studypals.domain.groupManage.dto.*;
import com.studypals.global.request.Cursor;
import com.studypals.global.responses.CursorResponse;

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

    /**
     * 그룹장이 그룹에 들어온 가입 요청 목록을 조회합니다.
     * 요청 ID를 기반으로 하는 커서를 통해 페이징하여 조회합니다.
     * <p>
     * 조회 시점으로부터 일주일이 지난 요청은 조회 시점에 자동으로 삭제됩니다.
     * <p>
     * 요청한 사용자가 그룹장이 아닐 경우 권한 없음 예외가 발생합니다.
     *
     * @param userId 사용자 ID
     * @param groupId 조회할 그룹 ID
     * @param cursor 조회할 {@link Cursor} 조건
     * @return {@link GroupEntryRequestDto}의 {@link CursorResponse}
     */
    CursorResponse.Content<GroupEntryRequestDto> getEntryRequests(Long userId, Long groupId, Cursor cursor);

    /**
     * 그룹장이 그룹에 들어온 가입 요청을 승인합니다.
     * 요청한 사용자가 그룹장이 아닐 경우 권한 없음 예외가 발생합니다.
     *
     * @param userId 사용자 ID
     * @param requestId 승인할 요청 ID
     * @return {@link AcceptEntryRes}
     */
    AcceptEntryRes acceptEntryRequest(Long userId, Long requestId);

    /**
     * 그룹장이 그룹에 들어온 가입 요청을 거절합니다.
     * 요청한 사용자가 그룹장이 아닐 경우 권한 없음 예외가 발생합니다.
     *
     * @param userId 사용자 ID
     * @param requestId 거절할 가입 요청 ID
     */
    void refuseEntryRequest(Long userId, Long requestId);
}
