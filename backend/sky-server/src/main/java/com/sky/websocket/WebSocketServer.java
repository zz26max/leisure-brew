package com.sky.websocket;

import com.sky.constant.JwtClaimsConstant;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/{employeeId}")
@Slf4j
public class WebSocketServer {

    private static final Map<String, Session> SESSIONS = new ConcurrentHashMap<>();
    private static volatile String adminSecretKey;

    public static void configure(String secretKey) {
        adminSecretKey = secretKey;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("employeeId") String employeeId) {
        Claims claims = parseAdminClaims(firstParameter(session, "token"));
        String authenticatedEmployeeId = claimAsString(claims, JwtClaimsConstant.EMP_ID);
        if (!employeeId.equals(authenticatedEmployeeId)) {
            close(session, CloseReason.CloseCodes.VIOLATED_POLICY, "身份校验失败");
            return;
        }

        session.getUserProperties().put("employeeId", authenticatedEmployeeId);
        session.getUserProperties().put("expiresAt", claims.getExpiration());
        SESSIONS.put(session.getId(), session);
        log.info("管理端实时连接已建立，employeeId={}，在线连接={}",
                authenticatedEmployeeId, SESSIONS.size());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        if (!isAuthorized(session)) {
            close(session, CloseReason.CloseCodes.VIOLATED_POLICY, "登录已过期");
            return;
        }
        log.debug("收到管理端实时消息，employeeId={}，长度={}",
                session.getUserProperties().get("employeeId"), message == null ? 0 : message.length());
    }

    @OnClose
    public void onClose(Session session) {
        SESSIONS.remove(session.getId());
        log.info("管理端实时连接已关闭，在线连接={}", SESSIONS.size());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        if (session != null) {
            SESSIONS.remove(session.getId());
        }
        log.warn("管理端实时连接异常，sessionId={}",
                session == null ? null : session.getId(), throwable);
    }

    public void sendToAllClient(String message) {
        for (Session session : new ArrayList<>(SESSIONS.values())) {
            if (!isAuthorized(session)) {
                close(session, CloseReason.CloseCodes.VIOLATED_POLICY, "登录已过期");
                continue;
            }
            try {
                session.getAsyncRemote().sendText(message);
            } catch (RuntimeException ex) {
                SESSIONS.remove(session.getId());
                log.warn("实时消息发送失败，sessionId={}", session.getId(), ex);
            }
        }
    }

    public int getOnlineCount() {
        return SESSIONS.size();
    }

    private String firstParameter(Session session, String name) {
        List<String> values = session.getRequestParameterMap().get(name);
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    private Claims parseAdminClaims(String token) {
        if (adminSecretKey == null || token == null || token.trim().isEmpty()) {
            return null;
        }
        try {
            return JwtUtil.parseJWT(adminSecretKey, token);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private String claimAsString(Claims claims, String name) {
        if (claims == null || claims.get(name) == null) {
            return null;
        }
        return claims.get(name).toString();
    }

    private boolean isAuthorized(Session session) {
        if (session == null || !session.isOpen()
                || session.getUserProperties().get("employeeId") == null) {
            return false;
        }
        Object expiresAt = session.getUserProperties().get("expiresAt");
        return !(expiresAt instanceof Date) || ((Date) expiresAt).after(new Date());
    }

    private void close(Session session, CloseReason.CloseCode code, String reason) {
        if (session == null) {
            return;
        }
        SESSIONS.remove(session.getId());
        try {
            if (session.isOpen()) {
                session.close(new CloseReason(code, reason));
            }
        } catch (IOException ex) {
            log.debug("关闭实时连接失败，sessionId={}", session.getId(), ex);
        }
    }
}
