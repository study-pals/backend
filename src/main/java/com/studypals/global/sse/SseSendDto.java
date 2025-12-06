package com.studypals.global.sse;

/**
 * sse 로 메시지 전송을 요청할 때 사용하는 dto 객체입니다. <br>
 * 목적지 데이터는 포함되지 않으며 ,type 및 content 필드만 정의됩니다.
 * <p>
 *
 * <p><b>외부 모듈:</b><br>
 * SseEmitter
 *
 * @author jack8
 * @since 2025-12-04
 */
public record SseSendDto(String type, Object content) {}
