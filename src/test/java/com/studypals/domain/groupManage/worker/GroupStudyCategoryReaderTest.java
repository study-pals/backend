package com.studypals.domain.groupManage.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.groupManage.dao.GroupStudyCategoryRepository;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupStudyCategory;

/**
 * {@link GroupStudyCategoryReader} 에 대한 unit test 입니다.
 *
 * @author s0o0bn
 * @since 2025-05-21
 */
@ExtendWith(MockitoExtension.class)
public class GroupStudyCategoryReaderTest {

    @Mock
    private GroupStudyCategoryRepository groupStudyCategoryRepository;

    @Mock
    private Group mockGroup;

    @InjectMocks
    private GroupStudyCategoryReader groupStudyCategoryReader;

    @Test
    void getByGroup_success() {
        // given
        List<GroupStudyCategory> categories =
                List.of(GroupStudyCategory.builder().group(mockGroup).build());

        given(mockGroup.getId()).willReturn(1L);
        given(groupStudyCategoryRepository.findByGroupId(mockGroup.getId())).willReturn(categories);

        // when
        List<GroupStudyCategory> actual = groupStudyCategoryReader.getByGroup(mockGroup);

        // then
        assertThat(actual).isEqualTo(categories);
    }
}
