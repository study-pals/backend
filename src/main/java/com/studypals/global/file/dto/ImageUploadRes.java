package com.studypals.global.file.dto;

/**
 * 이미지 업로드 완료 후 반환되는 응답 DTO입니다.
 *
 * @param imageId       데이터베이스에 저장된 이미지의 고유 ID (PK).
 *                 추후 비즈니스 로직(예: 회원 정보 수정, 채팅 메시지 전송)에서 이 ID를 참조합니다.
 * @param imageUrl 업로드된 이미지를 즉시 조회할 수 있는 URL.
 *                 <p>
 *                 스토리지 설정에 따라 다음 중 하나가 반환됩니다:
 *                 <ul>
 *                     <li>Public 버킷인 경우: 영구적인 정적 URL</li>
 *                     <li>Private 버킷인 경우: 일정 시간 동안 유효한 GET Presigned URL</li>
 *                 </ul>
 * @author sleepyhoon
 * @since 2026-01-15
 */
public record ImageUploadRes(Long imageId, String imageUrl) {}
