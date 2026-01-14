package com.studypals.global.file.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatPresignedUrlReq(
        @NotNull(message = "파일 이름은 필수입니다.") @NotBlank(message = "파일 이름은 공백일 수 없습니다.") String fileName,
        @NotNull(message = "채팅방 ID는 필수입니다.") @NotBlank(message = "채팅방 ID는 공백일 수 없습니다.") String chatRoomId) {}
