package com.studypals.domain.groupManage.worker;

import java.util.List;

import org.springframework.data.domain.Slice;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dao.GroupTagRepository;
import com.studypals.domain.groupManage.dao.groupRepository.GroupRepository;
import com.studypals.domain.groupManage.dao.groupRepository.GroupSortType;
import com.studypals.domain.groupManage.dto.GroupSearchDto;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupTag;
import com.studypals.global.annotations.Worker;
import com.studypals.global.exceptions.errorCode.GroupErrorCode;
import com.studypals.global.exceptions.exception.GroupException;
import com.studypals.global.request.Cursor;

/**
 * group 도메인의 조회 Worker 클래스입니다.
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

    public Slice<Group> search(GroupSearchDto dto, Cursor cursor) {
        GroupSortType sortType = (GroupSortType) cursor.sort();
        try {
            sortType.getParser().apply(cursor.value());
        } catch (RuntimeException e) {
            throw new GroupException(
                    GroupErrorCode.GROUP_SEARCH_FAIL,
                    "value 타입 문자열 형식이 올바르지 않습니다.",
                    "[GroupReader#search] parsing value fail");
        }
        return groupRepository.search(dto, cursor);
    }
}
