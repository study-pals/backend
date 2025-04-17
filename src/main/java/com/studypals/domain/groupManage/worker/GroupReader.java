package com.studypals.domain.groupManage.worker;

import java.util.List;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dao.GroupRepository;
import com.studypals.domain.groupManage.dao.GroupTagRepository;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupTag;
import com.studypals.global.annotations.Worker;
import com.studypals.global.exceptions.errorCode.GroupErrorCode;
import com.studypals.global.exceptions.exception.GroupException;

/**
 * group 도메인의 기본 Worker 클래스입니다.
 *
 * <p>group 관련 CUD 로직을 수행합니다.
 *
 *
 * <p><b>빈 관리:</b><br>
 * Worker
 *
 * @author s0o0bn
 * @since 2025-04-15
 */
@Worker
@RequiredArgsConstructor
public class GroupReader {
    private final GroupRepository groupRepository;
    private final GroupTagRepository groupTagRepository;

    public List<GroupTag> getGroupTags() {
        return groupTagRepository.findAll();
    }

    public Group getById(Long groupId) {
        return groupRepository.findById(groupId).orElseThrow(() -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND));
    }
}
