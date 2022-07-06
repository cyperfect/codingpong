package com.ms.pong.customer;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;
import com.ms.pong.model.MessageVo;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 事件处理
 */
@Component
public class FileCustomer implements EventHandler<MessageVo>, WorkHandler<MessageVo> {
    @Override
    public void onEvent(MessageVo messageVo, long l, boolean b) throws Exception {
        this.onEvent(messageVo);
    }
    @Override
    public void onEvent(MessageVo messageVo) throws Exception {
        //这里做具体的消费逻辑
        LocalDateTime localDateTime = LocalDateTime.now();
        long timeInSeconds = localDateTime.toEpochSecond(ZoneOffset.UTC);
        System.out.println("消费:time--" + timeInSeconds + ";fileName--" + messageVo.getFileName() + ";text--" + messageVo.getText());
    }
}
