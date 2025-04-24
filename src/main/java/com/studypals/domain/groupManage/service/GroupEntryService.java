package com.studypals.domain.groupManage.service;

import com.studypals.domain.groupManage.dto.GroupEntryInfo;

public interface GroupEntryService {

    Long joinGroup(Long userId, GroupEntryInfo entryInfo);
}
