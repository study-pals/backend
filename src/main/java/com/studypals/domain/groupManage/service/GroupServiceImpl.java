package com.studypals.domain.groupManage.service;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dao.GroupMemberRepository;
import com.studypals.domain.groupManage.dao.GroupRepository;
import com.studypals.domain.groupManage.dto.CreateGroupReq;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupMember;
import com.studypals.domain.memberManage.dao.MemberRepository;
import com.studypals.domain.memberManage.entity.Member;

/**
 * group service 의 구현 클래스입니다.
 *
 * <p>group 도메인에 대한 전반적인 로직을 수행합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link GroupService} 의 구현 클래스입니다.
 *
 * <p><b>빈 관리:</b><br>
 * Service
 *
 * @author s0o0bn
 * @see GroupService
 * @since 2025-04-12
 */
@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {
    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    /**
     * 그룹을 생성하고 생성한 사용자에 그룹장 권한을 부여합니다.
     *
     * @param userId 그룹을 생성할 사용자
     * @param dto 그룹 생성 시 필요한 데이터
     * @return 생성된 그룹 ID
     */
    @Override
    @Transactional
    public Long createGroup(Long userId, CreateGroupReq dto) {
        Group group = dto.toEntity();
        groupRepository.save(group);

        Member creator = memberRepository.getReferenceById(userId);
        GroupMember leader = GroupMember.createLeader(creator, group);

        return groupMemberRepository.save(leader).getId();
    }
}
