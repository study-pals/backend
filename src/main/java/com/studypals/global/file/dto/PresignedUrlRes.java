package com.studypals.global.file.dto;

/**
 * Presigned URL 생성 요청에 대한 응답 데이터를 담는 DTO(Data Transfer Object)입니다.
 * <p>
 * 이 객체는 클라이언트가 파일을 스토리지에 직접 업로드하는 데 필요한 정보들을 제공합니다.
 * 클라이언트는 이 응답을 받아 {@code url}에 파일을 업로드한 후, 필요에 따라 {@code id}를 통해
 * 서버에 업로드 완료를 통지하여 이미지 상태를 변경할 수 있습니다.
 *
 * @param id  데이터베이스에 미리 저장된 이미지 메타데이터의 고유 ID.
 *            파일 업로드 완료 후 서버에 상태 변경(예: PENDING -> COMPLETE)을 알리는 데 사용됩니다.
 * @param url 파일을 업로드할 수 있는, 유효 기간이 제한된 Presigned URL (일반적으로 HTTP PUT).
 *            클라이언트는 이 URL 주소로 파일 데이터를 직접 전송해야 합니다.
 * @author sleepyhoon
 * @since 2024-01-15
 */
public record PresignedUrlRes(Long id, String url) {}
