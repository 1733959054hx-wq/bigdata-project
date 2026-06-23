package com.gzu.bigdata.websocket;

import org.springframework.stereotype.Component;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint("/websocket/chat")
public class ChatWebSocket {

    private static final CopyOnWriteArraySet<ChatWebSocket> clients = new CopyOnWriteArraySet<>();
    private Session session;

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        clients.add(this);
        try {
            sendMessage("欢迎加入聊天室！当前在线人数: " + clients.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("WebSocket已连接: " + session.getId() + ", 当前连接数: " + clients.size());
    }

    @OnClose
    public void onClose() {
        clients.remove(this);
        System.out.println("WebSocket已关闭: " + (session != null ? session.getId() : "unknown"));
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("收到来自 " + session.getId() + " 的消息: " + message);

        // 广播消息给所有客户端
        for (ChatWebSocket client : clients) {
            try {
                client.sendMessage("用户 " + session.getId() + " 说: " + message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.err.println("WebSocket错误: " + session.getId());
        error.printStackTrace();
    }

    private void sendMessage(String message) throws IOException {
        if (this.session != null && this.session.isOpen()) {
            this.session.getBasicRemote().sendText(message);
        }
    }
}