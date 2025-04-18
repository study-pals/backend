package com.studypals.domain.groupManage.worker;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.groupManage.dao.GroupEntryCodeRedisRepository;

@ExtendWith(MockitoExtension.class)
public class GroupEntryCodeManagerTest {

    @Mock
    private GroupEntryCodeRedisRepository entryCodeRepository;

    @InjectMocks
    private GroupEntryCodeManager entryCodeManager;

    @Test
    void generate_success() {
        // given
        Long groupId = 1L;

        // when
        String code = entryCodeManager.generate(groupId);

        // then
        assertThat(code).isNotNull();
        assertThat(code).hasSize(6);
        assertThat(code).matches("[A-Z0-9]+");
    }
}
