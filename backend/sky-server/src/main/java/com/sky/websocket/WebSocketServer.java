package com.sky.websocket;

import lombok.extern.slf4j.Slf4j;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket服务
 */
@ServerEndpoint("/ws/{sid}")
@Slf4j
public class WebSocketServer {

    private static final Map<String, Session> sessionMap = new ConcurrentHashMap<>();
    private static WebSocketServer instance;

    public WebSocketServer() {
        instance = this;
    }

    public static WebSocketServer getInstance() {
        return instance;
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        log.info("客户端：{} 建立连接", sid);
        sessionMap.put(sid, session);
        log.info("当前WebSocket在线连接数：{}", sessionMap.size());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        log.info("收到来自客户端：{} 的信息:{}", sid, message);
    }

    /**
     * 连接关闭调用的方法
     *
     * @param sid
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        log.info("连接断开:{}", sid);
        sessionMap.remove(sid);
        log.info("当前WebSocket在线连接数：{}", sessionMap.size());
    }

    @OnError
    public void onError(Session session, Throwable throwable, @PathParam("sid") String sid) {
        log.error("WebSocket连接异常，sid={}, sessionId={}", sid, session == null ? null : session.getId(), throwable);
    }

    /**
     * 群发
     *
     * @param message
     */
    public void sendToAllClient(String message) {
        if (sessionMap.isEmpty()) {
            log.warn("WebSocket推送失败：当前无在线管理端连接，消息:{}", message);
            return;
        }
        Collection<Session> sessions = sessionMap.values();
        for (Session session : sessions) {
            try {
                // !仅向仍然可用的连接推送，避免关闭连接导致消息发送失败
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(message);
                }
            } catch (Exception e) {
                log.error("WebSocket消息推送异常", e);
            }
        }
    }

    public int getOnlineCount() {
        return sessionMap.size();
    }

}
