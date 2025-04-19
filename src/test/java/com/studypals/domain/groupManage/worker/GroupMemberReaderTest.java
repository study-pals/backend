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
import com.studypals.domain.groupManage.dto.GroupMemberProfileImageDto;
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
        List<GroupMemberProfileImageDto> profiles = List.of(
                new GroupMemberProfileImageDto("imageUrl url", GroupRole.LEADER),
                new GroupMemberProfileImageDto("imageUrl url", GroupRole.MEMBER));

        given(groupMemberRepository.findTopNMemberByJoinedAt(groupId, limit)).willReturn(profiles);

        // when
        List<GroupMemberProfileImageDto> actual = groupMemberReader.getTopNMemberProfiles(groupId, limit);

        // then
        assertThat(actual).isEqualTo(profiles);
    }
}
