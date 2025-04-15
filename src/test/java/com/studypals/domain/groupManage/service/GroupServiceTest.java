package com.studypals.domain.groupManage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.groupManage.dto.CreateGroupReq;
import com.studypals.domain.groupManage.dto.GetGroupTagRes;
import com.studypals.domain.groupManage.dto.mappers.GroupMapper;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupTag;
import com.studypals.domain.groupManage.worker.GroupFinder;
import com.studypals.domain.groupManage.worker.GroupMemberWorker;
import com.studypals.domain.groupManage.worker.GroupWorker;
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
    private GroupWorker groupWorker;

    @Mock
    private GroupFinder groupFinder;

    @Mock
    private GroupMemberWorker groupMemberWorker;

    @Mock
    private GroupMapper groupMapper;

    @Mock
    private Group mockGroup;

    @Mock
    private GroupTag mockGroupTag;

    @InjectMocks
    private GroupServiceImpl groupService;

    @Test
    void getGroupTags_success() {
        // given
        GetGroupTagRes res = new GetGroupTagRes(mockGroupTag.getName());

        given(groupFinder.getGroupTags()).willReturn(List.of(mockGroupTag));
        given(groupMapper.toTagDto(mockGroupTag)).willReturn(res);

        // when
        List<GetGroupTagRes> actual = groupService.getGroupTags();

        // then
        assertThat(actual).isEqualTo(List.of(res));
    }

    @Test
    void createGroup_success() {
        // given
        Long userId = 1L;
        CreateGroupReq req = new CreateGroupReq("group name", "group tag", 10, false, false);

        given(groupWorker.create(req)).willReturn(mockGroup);

        // when
        Long actual = groupService.createGroup(userId, req);

        // then
        assertThat(actual).isEqualTo(mockGroup.getId());
    }

    @Test
    void createGroup_fail_whileGroupCreating() {
        // given
        Long userId = 1L;
        GroupErrorCode errorCode = GroupErrorCode.GROUP_CREATE_FAIL;
        CreateGroupReq req = new CreateGroupReq("group name", "group tag", 10, false, false);

        given(groupWorker.create(req)).willThrow(new GroupException(errorCode));

        // when & then
        assertThatThrownBy(() -> groupService.createGroup(userId, req))
                .isInstanceOf(GroupException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }

    @Test
    void createGroup_fail_whileGroupMemberCreating() {
        // given
        Long userId = 1L;
        GroupErrorCode errorCode = GroupErrorCode.GROUP_MEMBER_CREATE_FAIL;
        CreateGroupReq req = new CreateGroupReq("group name", "group tag", 10, false, false);

        given(groupWorker.create(req)).willReturn(mockGroup);
        given(groupMemberWorker.createLeader(userId, mockGroup)).willThrow(new GroupException(errorCode));

        // when & then
        assertThatThrownBy(() -> groupService.createGroup(userId, req))
                .isInstanceOf(GroupException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }
}
