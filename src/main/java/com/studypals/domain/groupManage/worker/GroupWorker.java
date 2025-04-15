package com.studypals.domain.groupManage.worker;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dao.GroupRepository;
import com.studypals.domain.groupManage.dao.GroupTagRepository;
import com.studypals.domain.groupManage.dto.CreateGroupReq;
import com.studypals.domain.groupManage.dto.mappers.GroupMapper;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.global.exceptions.errorCode.GroupErrorCode;
import com.studypals.global.exceptions.exception.GroupException;

/**
 * group 도메인의 조회용 Worker 클래스입니다.
 *
 * <p>group 관련 조회 로직을 수행합니다.
 *
 *
 * <p><b>빈 관리:</b><br>
 * Worker
 *
 * @author s0o0bn
 * @since 2025-04-15
 */
@Component
@RequiredArgsConstructor
public class GroupWorker {
    private final GroupRepository groupRepository;
    private final GroupTagRepository groupTagRepository;
    private final GroupMapper groupMapper;

    public Group create(CreateGroupReq dto) {
        Group group = groupMapper.toEntity(dto);
        if (!groupTagRepository.existsById(dto.tag())) {
            throw new GroupException(GroupErrorCode.GROUP_CREATE_FAIL, "no such tag.");
        }

        try {
            groupRepository.save(group);
        } catch (Exception e) {
            throw new GroupException(GroupErrorCode.GROUP_CREATE_FAIL);
        }

        return group;
    }
}
