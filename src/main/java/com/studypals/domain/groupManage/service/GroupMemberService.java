package com.studypals.domain.groupManage.service;

/**
 * GroupMemberService 의 인터페이스입니다. 메서드를 정의합니다.
 *
 * <p>
 *
 * <p><b>상속 정보:</b><br>
 * GroupMemberServiceImpl의 부모 인터페이스입니다.
 *
 * @author zjxlomin
 * @see GroupMemberServiceImpl
 * @since 2026-01-13
 */
public interface GroupMemberService {
    /**
     * 그룹장이 다른 멤버에게 권한을 넘겨줍니다.
     * @param groupId 소속 그룹
     * @param userId 그룹장을 넘겨줄 사용자
     * @param nextLeaderId 그룹장을 받을 멤버
     * @throws com.studypals.global.exceptions.exception.GroupException
     */
    void promoteLeader(Long groupId, Long userId, Long nextLeaderId);
}
