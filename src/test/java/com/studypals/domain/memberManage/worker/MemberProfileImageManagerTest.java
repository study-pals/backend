package com.studypals.domain.memberManage.worker;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.global.file.FileProperties;
import com.studypals.global.file.ObjectStorage;
import com.studypals.global.file.entity.ImageType;

@ExtendWith(MockitoExtension.class)
class MemberProfileImageManagerTest {

    @Mock
    private ObjectStorage objectStorage;

    private MemberProfileImageManager memberProfileImageManager;

    @BeforeEach
    void setUp() {
        FileProperties fileUploadProperties = new FileProperties(List.of("jpg", "png"), 600);
        memberProfileImageManager = new MemberProfileImageManager(objectStorage, fileUploadProperties);
    }

    @Test
    @DisplayName("파일 타입 반환 확인")
    void getFileType() {
        // when
        ImageType type = memberProfileImageManager.getFileType();

        // then
        assertThat(type).isEqualTo(ImageType.PROFILE_IMAGE);
    }
}
