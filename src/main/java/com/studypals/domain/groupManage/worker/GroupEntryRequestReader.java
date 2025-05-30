package com.studypals.domain.groupManage.worker;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dao.GroupEntryRequestRepository;
import com.studypals.domain.groupManage.entity.GroupEntryRequest;
import com.studypals.global.annotations.Worker;
import com.studypals.global.exceptions.errorCode.GroupErrorCode;
import com.studypals.global.exceptions.exception.GroupException;

/**
 * group entry request 도메인의 조회 Worker 클래스입니다.
 *
 * <p>group entry request 관련 조회 로직을 수행합니다.
 *
 *
 * <p><b>빈 관리:</b><br>
 * Worker
 *
 * @author s0o0bn
 * @since 2025-05-27
 */
@Worker
@RequiredArgsConstructor
public class GroupEntryRequestReader {
    private final GroupEntryRequestRepository entryRequestRepository;

    public GroupEntryRequest getById(Long requestId) {
        return entryRequestRepository
                .findById(requestId)
                .orElseThrow(() -> new GroupException(GroupErrorCode.GROUP_ENTRY_REQUEST_NOT_FOUND));
    }
}
