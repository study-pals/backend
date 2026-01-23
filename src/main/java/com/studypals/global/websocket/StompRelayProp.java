package com.studypals.global.websocket;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Stomp relay 용 메시지 브로커 연결 설정
 *
 * @author jack8
 * @see WebsocketConfig
 * @since 2026-01-14
 */
@ConfigurationProperties(prefix = "stomp.relay")
public record StompRelayProp(
        String host,
        Integer port,
        String username,
        String password,
        String virtualHost,
        Integer systemHeartbeatSendInterval,
        Integer systemHeartbeatReceiveInterval) {}
