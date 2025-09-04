package com.app.backend.interceptor;

import com.app.backend.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RequiredArgsConstructor
@Slf4j
public class WebSocketInterceptor implements ChannelInterceptor {

    private final TokenProvider tokenProvider;
    private static final String BEARER_PREFIX = "Bearer ";

    @SneakyThrows
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authToken = accessor.getFirstNativeHeader("Authorization");

            if (authToken != null && authToken.startsWith(BEARER_PREFIX)) {
                String token = authToken.substring(BEARER_PREFIX.length());

                if (tokenProvider.validateToken(token)) {
                    // JWT 토큰이 유효한 경우에만 Authentication 객체를 생성
                    Authentication authentication = tokenProvider.getAuthentication(token);
                    accessor.setUser(authentication);

                    // SecurityContextHolder에 Authentication 객체를 설정
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    log.error("Invalid JWT token: {}", token);
                    throw new IllegalArgumentException("Invalid JWT token");
                }
            } else {
                log.error("Invalid Authorization header: {}", authToken);
                throw new IllegalArgumentException("Invalid Authorization header");
            }
        }

        return message;
    }
}
