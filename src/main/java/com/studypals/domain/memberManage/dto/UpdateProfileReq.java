package com.studypals.domain.memberManage.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.PastOrPresent;

/**
 * 사용자의 프로필을 입력/갱신하기 위해 사용하는 DTO 입니다.
 * <p>
 * 최초 회원가입 시 필수 정보 및 부가 정보 API 를 분리하였고, 부가 정보 입력 시에도 사용됩니다.
 *
 * @author jack8
 * @since 2025-12-16
 */
public record UpdateProfileReq(@PastOrPresent LocalDate birthday, String position) {}
