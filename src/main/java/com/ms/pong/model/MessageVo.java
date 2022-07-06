package com.ms.pong.model;

import com.lmax.disruptor.WorkHandler;

public class MessageVo {

    private String text;
    private String fileName;

    public MessageVo() {}

    public MessageVo(String text, String fileName) {
        this.text = text;
        this.fileName = fileName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
