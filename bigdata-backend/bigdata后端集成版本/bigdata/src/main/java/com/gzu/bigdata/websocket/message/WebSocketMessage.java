package com.gzu.bigdata.websocket.message;

import lombok.Data;
import java.util.Map;

@Data
public class WebSocketMessage {
    private String type;        // 消息类型：edit, cursor, sync, join, leave, awareness
    private String documentId;  // 文档ID
    private String userId;      // 用户ID
    private Object payload;     // 消息内容
    private Long timestamp;     // 时间戳
}