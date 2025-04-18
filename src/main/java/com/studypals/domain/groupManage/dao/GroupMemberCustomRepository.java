package com.studypals.domain.groupManage.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.studypals.domain.groupManage.dto.GroupMemberProfileDto;

@Repository
public interface GroupMemberCustomRepository {

    /**
     * 그룹 대표 N명의 멤버를 조회합니다.
     * 이때 그룹장은 무조건 포함합니다.
     *
     * @param groupId 조회할 그룹 ID
     * @return 그룹장 포함 대표 멤버 리스트
     * @see GroupMemberProfileDto
     */
    List<GroupMemberProfileDto> findTopNMember(Long groupId, int limit);
}
