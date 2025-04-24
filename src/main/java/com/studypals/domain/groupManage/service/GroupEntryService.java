package com.studypals.domain.groupManage.service;

import com.studypals.domain.groupManage.dto.GroupEntryInfo;

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
     * 공개 그룹에 승인 없이 바로 가입합니다.
     *
     * @param userId 가입할 사용자 ID
     * @param entryInfo 가입할 그룹 정보 {@link GroupEntryInfo}
     * @return {@link com.studypals.domain.groupManage.entity.GroupMember} ID
     */
    Long joinGroup(Long userId, GroupEntryInfo entryInfo);

    /**
     * 비공개 그룹에 가입 요청을 보냅니다.
     *
     * @param userId 요청할 사용자 ID
     * @param entryInfo 요청할 그룹 정보 {@link GroupEntryInfo}
     * @return {@link com.studypals.domain.groupManage.entity.GroupEntryRequest} ID
     */
    Long requestParticipant(Long userId, GroupEntryInfo entryInfo);
}
