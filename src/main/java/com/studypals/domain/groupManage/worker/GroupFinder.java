package com.studypals.domain.groupManage.worker;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dao.GroupRepository;
import com.studypals.domain.groupManage.dao.GroupTagRepository;
import com.studypals.domain.groupManage.entity.GroupTag;

@Component
@RequiredArgsConstructor
public class GroupFinder {
    private final GroupRepository groupRepository;
    private final GroupTagRepository groupTagRepository;

    public List<GroupTag> getGroupTags() {
        return groupTagRepository.findAll();
    }
}
