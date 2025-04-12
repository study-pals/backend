package com.studypals.domain.groupManage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.groupManage.dao.GroupMemberRepository;
import com.studypals.domain.groupManage.dao.GroupRepository;
import com.studypals.domain.groupManage.dto.CreateGroupReq;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.memberManage.dao.MemberRepository;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.global.exceptions.errorCode.GroupErrorCode;
import com.studypals.global.exceptions.exception.GroupException;

/**
 * {@link GroupService} 에 대한 단위 테스트입니다.
 *
 * <p>성공 케이스와 예외 케이스에 대한 테스트입니다.
 *
 * @author s0o0bn
 * @see GroupService
 * @since 2025-04-12
 */
@ExtendWith(MockitoExtension.class)
public class GroupServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @InjectMocks
    private GroupServiceImpl groupService;

    @Test
    void createGroup_success() {
        // given
        Long userId = 1L;
        CreateGroupReq req = new CreateGroupReq("group name", "group tag", 10, false, false);
        Group group = createGroup(req);

        given(memberRepository.getReferenceById(anyLong()))
                .willReturn(Member.builder().id(userId).build());
        given(groupRepository.save(any())).willReturn(group);

        // when
        Long actual = groupService.createGroup(userId, req);

        // then
        assertThat(actual).isEqualTo(group.getId());
    }

    @Test
    void createGroup_fail_whileGroupSave() {
        // given
        Long userId = 1L;
        GroupErrorCode errorCode = GroupErrorCode.GROUP_CREATE_FAIL;
        CreateGroupReq req = new CreateGroupReq("group name", "group tag", 10, false, false);
        Group group = createGroup(req);

        given(groupRepository.save(any())).willThrow(new GroupException(errorCode));

        // when & then
        assertThatThrownBy(() -> groupService.createGroup(userId, req))
                .isInstanceOf(GroupException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }

    @Test
    void createGroup_fail_whileGroupMemberSave() {
        // given
        Long userId = 1L;
        GroupErrorCode errorCode = GroupErrorCode.GROUP_CREATE_FAIL;
        CreateGroupReq req = new CreateGroupReq("group name", "group tag", 10, false, false);
        Group group = createGroup(req);

        given(memberRepository.getReferenceById(anyLong()))
                .willReturn(Member.builder().id(userId).build());
        given(groupRepository.save(any())).willReturn(group);
        given(groupMemberRepository.save(any())).willThrow(new GroupException(errorCode));

        // when & then
        assertThatThrownBy(() -> groupService.createGroup(userId, req))
                .isInstanceOf(GroupException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }

    private Group createGroup(CreateGroupReq req) {
        return Group.builder()
                .id(1L)
                .name(req.name())
                .tag(req.tag())
                .maxMember(req.maxMember())
                .isOpen(req.isOpen())
                .isApprovalRequired(req.isApprovalRequired())
                .build();
    }
}
