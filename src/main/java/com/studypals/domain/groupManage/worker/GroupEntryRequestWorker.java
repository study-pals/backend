package com.studypals.domain.groupManage.worker;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dao.GroupEntryRequestRepository;
import com.studypals.domain.groupManage.dto.mappers.GroupEntryRequestMapper;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupEntryRequest;
import com.studypals.domain.memberManage.dao.MemberRepository;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.global.annotations.Worker;

/**
 * group entry request 도메인의 기본 Worker 클래스입니다.
 *
 * <p>group entry request 관련 CUD 로직을 수행합니다.
 *
 *
 * <p><b>빈 관리:</b><br>
 * Worker
 *
 * @author s0o0bn
 * @since 2025-04-25
 */
@Worker
@RequiredArgsConstructor
public class GroupEntryRequestWorker {
    private MemberRepository memberRepository;
    private GroupEntryRequestRepository entryRequestRepository;
    private GroupEntryRequestMapper mapper;

    public GroupEntryRequest createRequest(Long userId, Group group) {
        Member member = memberRepository.getReferenceById(userId);
        GroupEntryRequest entryRequest = mapper.toEntity(member, group);
        entryRequestRepository.save(entryRequest);

        return entryRequest;
    }
}
