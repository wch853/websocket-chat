package com.njfu.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.njfu.chat.domain.ChatResponse;
import com.njfu.chat.enums.ResponseTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket处理器
 * 用于处理WebSocketSession的生命周期、单播消息、广播消息
 */
@Service
@EnableScheduling
public class ChatHandler implements WebSocketHandler {

    // 用于存放所有连接的WebSocketSession
    private static CopyOnWriteArraySet<WebSocketSession> webSocketSessions = new CopyOnWriteArraySet<>();

    // 用户存放所有在线用户信息
    private static CopyOnWriteArraySet<Map<String, Object>> sessionAttributes = new CopyOnWriteArraySet<>();

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private static final Logger log = LoggerFactory.getLogger(ChatHandler.class);

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 成功连接WebSocket后执行
     *
     * @param session session
     * @throws Exception Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 成功连接后将该连接加入集合
        webSocketSessions.add(session);
        sessionAttributes.add(session.getAttributes());
        log.info("session {} open, attributes: {}.", session.getId(), session.getAttributes());

        // 单播消息返回给该用户认证信息，httpSessionId是用户认证唯一标准
        this.unicast(session, ResponseTypeEnum.AUTHENTICATE.getKey());

        // 广播通知该用户上线
        this.broadcast(session, ResponseTypeEnum.ONLINE.getKey());

        // 广播刷新在线列表
        this.broadcast(ResponseTypeEnum.LIST.getKey(), sessionAttributes);
    }

    /**
     * 处理收到的WebSocketMessage，根据需求只处理TextMessage
     * （参照org.springframework.web.socket.handler.AbstractWebSocketHandler）
     *
     * @param session session
     * @param message message
     * @throws Exception Exception
     */
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            // 广播聊天信息
            this.broadcast(session, ResponseTypeEnum.CHAT.getKey(), ((TextMessage) message).getPayload());
        } else if (message instanceof BinaryMessage) {
            // 对BinaryMessage不作处理
        } else if (message instanceof PongMessage) {
            // 对PongMessage不作处理
        } else {
            throw new IllegalStateException("Unexpected WebSocket message type: " + message);
        }
    }

    /**
     * 处理WebSocketMessage transport error
     *
     * @param session   session
     * @param exception exception
     * @throws Exception Exception
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        // 对于异常连接，关闭并从webSocket移除Sessions中
        if (session.isOpen()) {
            session.close();
        }
        webSocketSessions.remove(session);
        sessionAttributes.remove(session.getAttributes());
        log.error("session {} error, errorMessage: {}.", session.getId(), exception.getMessage());

        // 广播异常掉线信息
        this.broadcast(session, ResponseTypeEnum.ERROR.getKey());

        // 广播刷新在线列表
        this.broadcast(ResponseTypeEnum.LIST.getKey(), sessionAttributes);
    }

    /**
     * 在两端WebSocket connection都关闭或transport error发生后执行
     *
     * @param session     session
     * @param closeStatus closeStatus
     * @throws Exception Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        boolean removeNow = webSocketSessions.remove(session);
        sessionAttributes.remove(session.getAttributes());
        log.info("session {} close, closeStatus: {}.", session.getId(), closeStatus);

        if (removeNow) {
            // 广播下线信息
            this.broadcast(session, ResponseTypeEnum.OFFLINE.getKey());
        }

        // 广播刷新在线列表
        this.broadcast(ResponseTypeEnum.LIST.getKey(), sessionAttributes);
    }

    /**
     * Whether the WebSocketHandler handles partial messages. If this flag is set to
     * {@code true} and the underlying WebSocket server supports partial messages,
     * then a large WebSocket message, or one of an unknown size may be split and
     * maybe received over multiple calls to
     * {@link #handleMessage(WebSocketSession, WebSocketMessage)}. The flag
     * {@link WebSocketMessage#isLast()} indicates if
     * the message is partial and whether it is the last part.
     */
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 封装response并转为json字符串
     *
     * @param session session
     * @param type    type
     * @param payload payload
     * @return json response
     * @throws Exception Exception
     */
    private String getResponse(WebSocketSession session, String type, Object payload) throws Exception {
        ChatResponse chatResponse;

        if (null == session) {
            chatResponse = new ChatResponse();
        } else {
            Map<String, Object> attributes = session.getAttributes();
            String httpSessionId = (String) attributes.get("httpSessionId");
            String host = (String) attributes.get("host");
            String username = (String) attributes.get("username");

            chatResponse = new ChatResponse(httpSessionId, host, username);
        }

        chatResponse.setType(type);
        chatResponse.setPayload(payload);

        // 转为json字符串
        return objectMapper.writeValueAsString(chatResponse);
    }

    /**
     * 向单个WebSocketSession单播消息
     *
     * @param session session
     * @param type    type
     * @param payload payload
     * @throws Exception Exception
     */
    private void unicast(WebSocketSession session, String type, Object payload) throws Exception {
        String response = this.getResponse(session, type, payload);
        session.sendMessage(new TextMessage(response));
    }

    /**
     * 单播系统消息
     *
     * @param session session
     * @param type    type
     * @throws Exception Exception
     */
    private void unicast(WebSocketSession session, String type) throws Exception {
        this.unicast(session, type, null);
    }

    /**
     * 因某个WebSocketSession变动，向所有连接的WebSocketSession广播消息
     *
     * @param session 变动的WebSocketSession
     * @param type    com.njfu.chat.enums.ResponseTypeEnum 消息类型
     * @param payload 消息内容
     * @throws Exception Exception
     */
    private void broadcast(WebSocketSession session, String type, Object payload) throws Exception {
        String response = this.getResponse(session, type, payload);

        // 广播消息
        for (WebSocketSession webSocketSession : webSocketSessions) {
            webSocketSession.sendMessage(new TextMessage(response));
        }
    }

    /**
     * 用于多播系统消息
     *
     * @param session session
     * @param type    type
     * @throws Exception Exception
     */
    private void broadcast(WebSocketSession session, String type) throws Exception {
        this.broadcast(session, type, null);
    }

    /**
     * 用于无差别广播消息
     *
     * @param type    type
     * @param payload payload
     * @throws Exception Exception
     */
    private void broadcast(String type, Object payload) throws Exception {
        this.broadcast(null, type, payload);
    }

    /**
     * 定时任务，每5分钟发送一次服务器时间
     * @throws Exception Exception
     */
    @Scheduled(cron = "0 0-59/5 * * * ?")
    private void sendServerTime() throws Exception {
        this.broadcast(ResponseTypeEnum.TIME.getKey(), simpleDateFormat.format(new Date()));
    }
}
