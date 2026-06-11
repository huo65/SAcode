package com.DB.DBmarket.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    //发送时间
    private LocalDateTime timeSlot;
    //发送人id
    private  String sender;
    //接受人id
    private String receiver;
    //信息内容
    private String content;
}
