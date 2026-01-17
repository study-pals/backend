package com.studypals.global.file.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 채팅 사진 업로드에 필요한 정보를 가집니다.
 *
 * @param fileName 업로드할 파일의 이름. {@code @NotNull}과 {@code @NotBlank} 제약조건이 적용됩니다.
 * @param chatRoomId 업로드할 파일이 속한 채팅방 id. {@code @NotNull}과 {@code @NotBlank} 제약조건이 적용됩니다.
 *
 * @author sleepyhoon
 * @since 2026-01-10
 */
public record ChatPresignedUrlReq(
        @NotNull(message = "파일 이름은 필수입니다.") @NotBlank(message = "파일 이름은 공백일 수 없습니다.") String fileName,
        @NotNull(message = "채팅방 ID는 필수입니다.") @NotBlank(message = "채팅방 ID는 공백일 수 없습니다.") String chatRoomId) {}
