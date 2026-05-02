package com.masterminds.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.masterminds.service.JwtService;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Autowired
	private JwtService jwtService; // Dynamically injected
	
	@Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefixes for messages sent FROM server TO client
        registry.enableSimpleBroker("/topic", "/queue", "/user");
        
        // Prefix for messages sent FROM client TO server
        registry.setApplicationDestinationPrefixes("/app");
        
        // For private 1-on-1 messages
        registry.setUserDestinationPrefix("/user");
    }
	
	@Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // The URL where the frontend connects
        registry.addEndpoint("/ws-veranda").setAllowedOriginPatterns("*").withSockJS();
    }

//	@Override
//	public void registerStompEndpoints(StompEndpointRegistry registry) {
//		// The URL your React app will use to connect
//		registry.addEndpoint("/ws-veranda").setAllowedOrigins("http://192.168.18.72:3000", "https://veranda-sigma.vercel.app")
//				.withSockJS(); // Fallback for browsers that don't support WebSockets
//	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(new ChannelInterceptor() {
			@Override
			public Message<?> preSend(Message<?> message, MessageChannel channel) {
				StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

				// Only check on the initial CONNECT attempt
				if (StompCommand.CONNECT.equals(accessor.getCommand())) {
					String authHeader = accessor.getFirstNativeHeader("Authorization");

					if (authHeader != null && authHeader.startsWith("Bearer ")) {
						String token = authHeader.substring(7);

						try {
							// DYNAMIC CHECK:
							// This uses the Secret Key from your application.properties
							String phoneNumber = jwtService.extractPhoneNumber(token);

							if (phoneNumber != null && jwtService.isTokenValid(token)) {
								// Logic for setting the user in the context goes here
								System.out.println("WebSocket Authenticated for: " + phoneNumber);
							}
						} catch (Exception e) {
							e.printStackTrace();
							// If token is expired or fake, the connection is rejected
							throw new MessageDeliveryException("Invalid JSON Web Token");
						}
					} else {
						throw new MessageDeliveryException("Missing Authorization Header");
					}
				}
				return message;
			}
		});
	}

}
