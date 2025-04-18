package com.studypals.domain.groupManage.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.groupManage.dao.GroupMemberRepository;
import com.studypals.domain.groupManage.dto.GroupMemberProfileDto;
import com.studypals.domain.groupManage.entity.GroupRole;

@ExtendWith(MockitoExtension.class)
public class GroupMemberReaderTest {

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @InjectMocks
    private GroupMemberReader groupMemberReader;

    @Test
    void getTopNMemberProfiles_success() {
        // given
        Long groupId = 1L;
        int limit = 2;
        List<GroupMemberProfileDto> profiles = List.of(
                new GroupMemberProfileDto("image url", GroupRole.LEADER),
                new GroupMemberProfileDto("image url", GroupRole.MEMBER));

        given(groupMemberRepository.findTopNMember(groupId, limit)).willReturn(profiles);

        // when
        List<GroupMemberProfileDto> actual = groupMemberReader.getTopNMemberProfiles(groupId, limit);

        // then
        assertThat(actual).isEqualTo(profiles);
    }
}
