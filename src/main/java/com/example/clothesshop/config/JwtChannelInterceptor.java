package com.example.clothesshop.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.util.List;

public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtChannelInterceptor(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null) {
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                // try Authorization header first
                List<String> auth = accessor.getNativeHeader("Authorization");
                String token = null;
                if (auth != null && !auth.isEmpty()) {
                    String header = auth.get(0);
                    if (header.startsWith("Bearer ")) {
                        token = header.substring(7);
                    }
                }

                // fallback to token native header
                if (token == null) {
                    List<String> toks = accessor.getNativeHeader("token");
                    if (toks != null && !toks.isEmpty()) {
                        token = toks.get(0);
                    }
                }

                if (token != null && jwtTokenProvider.validateToken(token)) {
                    String username = jwtTokenProvider.getUsername(token);
                    accessor.setUser(new StompPrincipal(username));
                }
            }
        }
        return message;
    }

}
