package com.studypals.domain.memberManage.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.studypals.global.file.ObjectStorage;
import com.studypals.global.file.entity.ImageType;

@ExtendWith(MockitoExtension.class)
class MemberProfileManagerTest {

    @Mock
    private ObjectStorage objectStorage;

    private MemberProfileManager memberProfileManager;

    @BeforeEach
    void setUp() {
        memberProfileManager = new MemberProfileManager(objectStorage);
        // 부모 클래스의 @Value 필드 주입
        ReflectionTestUtils.setField(memberProfileManager, "acceptableExtensions", List.of("jpg", "png"));
        ReflectionTestUtils.setField(memberProfileManager, "presignedUrlExpireTime", 600);
    }

    @Test
    @DisplayName("업로드 URL 발급 성공")
    void getUploadUrl_success() {
        // given
        Long userId = 1L;
        String fileName = "profile.jpg";
        String targetId = String.valueOf(userId);
        String expectedUrl = "https://example.com/presigned-url";

        given(objectStorage.createPresignedPutUrl(anyString(), anyInt())).willReturn(expectedUrl);

        // when
        String result = memberProfileManager.getUploadUrl(userId, fileName, targetId);

        // then
        assertThat(result).isEqualTo(expectedUrl);
    }

    @Test
    @DisplayName("파일 타입 반환 확인")
    void getFileType() {
        // when
        ImageType type = memberProfileManager.getFileType();

        // then
        assertThat(type).isEqualTo(ImageType.PROFILE_IMAGE);
    }
}
