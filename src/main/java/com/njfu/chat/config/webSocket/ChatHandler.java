package com.njfu.chat.config.webSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket处理器
 */
public class ChatHandler implements WebSocketHandler {

    // 用于存放所有连接的WebSocketSession
    public static CopyOnWriteArraySet<WebSocketSession> webSocketSessions = new CopyOnWriteArraySet<>();

    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * 成功连接WebSocket后执行
     *
     * @param session session
     * @throws Exception Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 成功连接后将该session加入集合
        webSocketSessions.add(session);
        log.info("session {} open.", session.getId());
    }

    /**
     * 处理收到的WebSocketMessage
     * （参照org.springframework.web.socket.handler.AbstractWebSocketHandler）
     *
     * @param session session
     * @param message message
     * @throws Exception Exception
     */
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            this.handleTextMessage(session, (TextMessage) message);
        } else if (message instanceof BinaryMessage) {
            // 对BinaryMessage不作处理
        } else if (message instanceof PongMessage) {
            // 对PongMessage不作处理
        } else {
            throw new IllegalStateException("Unexpected WebSocket message type: " + message);
        }
    }

    /**
     * 处理收到的文本信息，在本例中应为chat文本
     * @param session session
     * @param message message
     * @throws Exception Exception
     */
    private void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        session.sendMessage(message);
    }

    /**
     * 处理WebSocketMessage transport error
     * @param session session
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

        log.error("session {}, error: {}", session.getId(), exception.getMessage());
    }

    /**
     * 在两端WebSocket connection都关闭或transport error发生后执行
     *
     * @param session session
     * @param closeStatus closeStatus
     * @throws Exception Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        webSocketSessions.remove(session);
        log.info("session {} close, closeStatus: {}", session.getId(), closeStatus.getCode());
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
}
