package com.gzu.bigdata.websocket.message;
import lombok.Data;
import java.util.Map;
@Data
public class EditMessage {
    private String operation; // insert, delete
    private Integer position;
    private String text;
    private Map<String, Object> attributes;
}
