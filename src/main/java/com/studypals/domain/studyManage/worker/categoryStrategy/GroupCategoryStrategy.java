package com.studypals.domain.studyManage.worker.categoryStrategy;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dao.groupMemberRepository.GroupMemberRepository;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupMember;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyType;
import com.studypals.global.exceptions.errorCode.GroupErrorCode;
import com.studypals.global.exceptions.exception.GroupException;

/**
 * {@link CategoryStrategy} 에 대한 구현 클래스이자, {@link StudyType} 이 {@code StudyType.GROUP} 인 경우에 사용됩니다.
 *
 * <p><b>상속 정보:</b><br>
 * CategoryStrategy 에 대한 구현 클래스
 *
 * <p><b>빈 관리:</b><br>
 * Component 이며 {@link CategoryStrategyFactory} 에 List 로 주입됩니다.
 *
 * @author jack8
 * @see CategoryStrategyFactory
 * @see CategoryStrategy
 * @since 2025-08-04
 */
@Component
@RequiredArgsConstructor
public class GroupCategoryStrategy implements CategoryStrategy {

    private final GroupMemberRepository groupMemberRepository;

    /**
     * 타입을 반환합니다.
     * @return 지원하는 타입 바노한
     */
    @Override
    public StudyType getType() {
        return StudyType.GROUP;
    }

    /**
     * 해당 클래스가 매개변수 타입을 지원하는지 여부를 반환합니다.
     * @param type 알고자 하는 타입
     * @return boolean - 지원 여부
     */
    @Override
    public boolean supports(StudyType type) {
        return type == getType();
    }

    /**
     * typeId 에 대해, 사용자가 해당 그룹의 LEADER 인지를 검사합니다. LEADER 가 아니면 예외를 던집니다.
     * @param userId 생성 요청을 보낸 유저
     * @param typeId 만들 카테고리의 typeId
     */
    @Override
    public void validateToCreate(Long userId, Long typeId) {
        GroupMember groupMember = findGroupMember(userId, typeId);
        if (!groupMember.isLeader())
            throw new GroupException(
                    GroupErrorCode.GROUP_UPDATE_FAIL,
                    "[GroupCategoryStrategy#validateToCreate] only leader can create");
    }

    /**
     * 해당 사용자가 그룹에 속해 있는지 검증합니다. 속해있지 않으면 예외를 던집니다.
     * @param userId 읽을 사용자 아이디
     * @param studyCategory 읽을 엔티티
     */
    @Override
    public void validateToRead(Long userId, StudyCategory studyCategory) {
        findGroupMember(userId, studyCategory.getTypeId());
    }

    /**
     * 해당 사용자가 리더인지 검증합니다. 리더가 아니면 예외를 던집니다.
     * @param userId 갱신 요청한 유저 아이디
     * @param studyCategory 갱신할 카테고리 엔티티
     */
    @Override
    public void validateToWrite(Long userId, StudyCategory studyCategory) {
        GroupMember groupMember = findGroupMember(userId, studyCategory.getTypeId());
        if (!groupMember.isLeader())
            throw new GroupException(
                    GroupErrorCode.GROUP_UPDATE_FAIL, "[GroupCategoryStrategy#validateToWrite] only leader can write");
    }

    /**
     * userId 에 대해, 해당 유저가 속한 그룹 들의 아이디를 가져옵니다. 해당 정보를 Map 으로 반환합니다.
     * @param userId 검색할 유저 아이디
     * @return StudyType.GROUP 과 그에 따른 소속한 groupid 리스트
     */
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
