package com.kirchnersolutions.database.Servers.HTTP;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocketMessageBroker
@EnableScheduling
public class StompConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/queue", "/user", "/maint", "/table");
        //config.enableSimpleBroker("/maint");
        //config.enableSimpleBroker("/users");
        config.setApplicationDestinationPrefixes("/app");
        //config.setUserDestinationPrefix("/specific/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws");
        registry.addEndpoint("/ws").setAllowedOrigins("*").withSockJS().setInterceptors(HttpSessionHandshakeInterceptor());
    }

    @Bean
    public HttpSessionHandshakeInterceptor HttpSessionHandshakeInterceptor(){
        return new HttpSessionHandshakeInterceptor();
    }

    /*
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.setInterceptors(new ChannelInterceptorAdapter() {

            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {

                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);



                return message;
            }
        });
    }
*/
    /*
    @Bean
    public MessageHandler handle() {
    */
}