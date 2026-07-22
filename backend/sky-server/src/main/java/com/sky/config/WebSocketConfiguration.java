package com.sky.config;

import com.sky.properties.JwtProperties;
import com.sky.websocket.WebSocketServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfiguration {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Bean
    public WebSocketServer webSocketServer(JwtProperties jwtProperties) {
        WebSocketServer.configure(jwtProperties.getAdminSecretKey());
        return new WebSocketServer();
    }
}
