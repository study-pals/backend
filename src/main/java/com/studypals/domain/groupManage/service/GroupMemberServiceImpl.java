package com.studypals.domain.groupManage.service;

import com.studypals.domain.groupManage.worker.GroupMemberWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * group member service 의 구현 클래스입니다.
 *
 * <p>group member 도메인에 대한 전반적인 로직을 수행합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link GroupMemberService} 의 구현 클래스입니다.
 *
 * <p><b>빈 관리:</b><br>
 * Service
 *
 * @author zjxlomin
 * @see GroupMemberService
 * @since 2026-01-13
 */
@Service
@RequiredArgsConstructor
public class GroupMemberServiceImpl implements GroupMemberService {
    private final GroupMemberWriter groupMemberWriter;

    @Override
    @Transactional
    public void promoteLeader(Long groupId, Long userId, Long nextLeaderId){
        groupMemberWriter.promoteLeader(groupId, userId, nextLeaderId);
    }
}
