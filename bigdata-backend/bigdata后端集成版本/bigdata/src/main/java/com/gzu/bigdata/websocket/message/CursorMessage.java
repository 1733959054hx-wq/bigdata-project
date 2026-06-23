package com.gzu.bigdata.websocket.message;
import lombok.Data;
import java.util.Map;
@Data
public class CursorMessage {
    private Integer position;
    private String name;
    private String color;
}