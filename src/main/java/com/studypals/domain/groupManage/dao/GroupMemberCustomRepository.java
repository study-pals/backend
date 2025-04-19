package com.studypals.domain.groupManage.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.studypals.domain.groupManage.dto.GroupMemberProfileImageDto;

@Repository
public interface GroupMemberCustomRepository {

    /**
     * 그룹 대표 N명의 멤버를 조회합니다.
     * 이때 그룹장은 무조건 포함합니다.
     * 나머지 그룹원의 경우 가장 최근에 가입한 멤버를 조회합니다.
     *
     * @param groupId 조회할 그룹 ID
     * @param limit 조회할 그룹원 수
     * @return 그룹장 포함 대표 멤버 프로필 이미지 리스트
     * @see GroupMemberProfileImageDto
     */
    List<GroupMemberProfileImageDto> findTopNMemberByJoinedAt(Long groupId, int limit);
}
