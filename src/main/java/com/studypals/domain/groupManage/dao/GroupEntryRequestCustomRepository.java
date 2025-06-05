package com.studypals.domain.groupManage.dao;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.studypals.domain.groupManage.entity.GroupEntryRequest;
import com.studypals.global.request.Cursor;

@Repository
public interface GroupEntryRequestCustomRepository {
    Slice<GroupEntryRequest> findByGroupIdAndSortBy(Long groupId, Cursor cursor);
}
