package com.studypals.domain.studyManage.worker.categoryStrategy;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dao.GroupMemberRepository;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupMember;
import com.studypals.domain.studyManage.dao.StudyCategoryRepository;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyType;
import com.studypals.global.exceptions.errorCode.GroupErrorCode;
import com.studypals.global.exceptions.exception.GroupException;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ExampleClass(String example)}  <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author jack8
 * @see
 * @since 2025-08-04
 */
@Component
@RequiredArgsConstructor
public class GroupCategoryStrategy implements CategoryStrategy {

    private final GroupMemberRepository groupMemberRepository;
    private final StudyCategoryRepository studyCategoryRepository;

    @Override
    public StudyType getType() {
        return StudyType.GROUP;
    }

    @Override
    public boolean supports(StudyType type) {
        return type == getType();
    }

    @Override
    public void validateToCreate(Long userId, Long typeId) {
        GroupMember groupMember = findGroupMember(userId, typeId);
        if (!groupMember.isLeader())
            throw new GroupException(
                    GroupErrorCode.GROUP_UPDATE_FAIL,
                    "[GroupCategoryStrategy#validateToCreate] only leader can create");
    }

    // used in transaction
    @Override
    public void validateToRead(Long userId, StudyCategory studyCategory) {
        findGroupMember(userId, studyCategory.getTypeId());
    }

    // used in transaction
    @Override
    public void validateToWrite(Long userId, StudyCategory studyCategory) {
        GroupMember groupMember = findGroupMember(userId, studyCategory.getTypeId());
        if (!groupMember.isLeader())
            throw new GroupException(
                    GroupErrorCode.GROUP_UPDATE_FAIL, "[GroupCategoryStrategy#validateToWrite] only leader can write");
    }

    @Override
    public Map<StudyType, List<Long>> getMapByUserId(Long userId) {
        List<Long> groupIds = groupMemberRepository.findAllByMemberId(userId).stream()
                .map(GroupMember::getGroup)
                .map(Group::getId)
                .toList();

        return Map.of(getType(), groupIds);
    }

    private GroupMember findGroupMember(Long userId, Long groupId) {
        return groupMemberRepository
                .findByMemberIdAndGroupId(userId, groupId)
                .orElseThrow(() -> new GroupException(
                        GroupErrorCode.GROUP_MEMBER_NOT_FOUND,
                        "[GroupCategoryStrategy#findGroupMember] unkown user - group match"));
    }
}
