package com.app.backend.handler;

import com.app.backend.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    private final TokenProvider tokenProvider;

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // JWT 토큰을 통한 사용자 인증 로직 구현
        // 여기서는 간단히 null을 반환하지만, 실제로는 JWT 토큰을 파싱하여 사용자 정보를 반환해야 합니다.
        return null;
    }
}
