package com.studypals.global.file.dto;

/**
 * 이미지 파일 업로드 완료 후 반환되는 데이터 전송 객체(DTO)입니다.
 * <p>
 * 스토리지에 저장된 파일의 고유 식별자(Object Key)와
 * 클라이언트가 접근 가능한 URL 정보를 포함합니다.
 *
 * @param objectKey 스토리지에 저장된 객체의 고유 키 (예: "profile/1/uuid.jpg")
 * @param imageUrl  이미지에 접근할 수 있는 전체 URL (예: "http://cdn.example.com/profile/1/uuid.jpg")
 */
public record ImageUploadDto(String objectKey, String imageUrl) {}
