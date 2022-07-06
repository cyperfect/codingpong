package com.ms.pong.producer;

import com.lmax.disruptor.RingBuffer;
import com.ms.pong.model.MessageVo;

public class Producer {

    private RingBuffer<MessageVo> ringBuffer;

    public Producer(RingBuffer<MessageVo> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public RingBuffer<MessageVo> getRingBuffer() {
        return ringBuffer;
    }

    public void setRingBuffer(RingBuffer<MessageVo> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }
    public void setData(String fileName, String text) {

        long next = ringBuffer.next();
        try {
            MessageVo messageVo = ringBuffer.get(next);
            messageVo.setFileName(fileName);
            messageVo.setText(text);
            System.out.println("生产:" + messageVo.getFileName() + "--" + messageVo.getText());
        } finally {
            ringBuffer.publish(next);
        }

    }
}
