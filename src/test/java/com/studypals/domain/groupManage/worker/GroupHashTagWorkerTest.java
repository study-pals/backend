package com.studypals.domain.groupManage.worker;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.when;

import java.util.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.groupManage.dao.GroupHashTagRepository;
import com.studypals.domain.groupManage.dao.HashTagRepository;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupHashTag;
import com.studypals.domain.groupManage.entity.HashTag;
import com.studypals.global.utils.StringUtils;

/**
 * {@link GroupHashTagWorker} 에 대한 테스트코드
 *
 * @author jack8
 * @since 2026-01-05
 */
@ExtendWith(MockitoExtension.class)
class GroupHashTagWorkerTest {

    @Mock
    GroupHashTagRepository groupHashTagRepository;

    @Mock
    HashTagRepository hashTagRepository;

    @Spy
    StringUtils stringUtils = new StringUtils();

    @InjectMocks
    GroupHashTagWorker worker;

    @Test
    void saveTags_success() {
        // given
        Group group = Group.builder().id(1L).build();

        List<String> inputTags = List.of("#Spring Boot", "#JPA");

        // normalize 결과
        // "Spring Boot" -> "Spring_Boot"
        // "JPA" -> "JPA"

        HashTag existing = HashTag.builder().id(10L).tag("jpa").usedCount(3L).build();

        when(hashTagRepository.findAllByTagIn(Set.of("spring_boot", "jpa")))
                .thenReturn(new ArrayList<>(List.of(existing)));

        when(hashTagRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        worker.saveTags(group, inputTags);

        // then
        verify(hashTagRepository).findAllByTagIn(any());
        verify(hashTagRepository).increaseUsedCountBulk(List.of("jpa"));
        verify(hashTagRepository).saveAll(argThat(iterable -> {
            Collection<HashTag> tags = (Collection<HashTag>) iterable;
            return tags.size() == 1
                    && tags.iterator().next().getTag().equals("spring_boot")
                    && tags.iterator().next().getUsedCount() == 1L;
        }));

        verify(groupHashTagRepository).saveAll(argThat(iterable -> {
            Collection<GroupHashTag> relations = (Collection<GroupHashTag>) iterable;
            return relations.size() == 2 && relations.stream().allMatch(r -> r.getGroup() == group);
        }));
    }
}
