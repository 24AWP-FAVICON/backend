package com.example.demo.entity.messenger;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private String type;
    private String sender;
    private String channelId;
    private Object data;

    public void newConnect() {
        this.type = "new";
    }

    public void closeConnect(){
        this.type = "close";
    }
}
