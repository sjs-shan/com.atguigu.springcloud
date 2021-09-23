package com.atguigu.springcloud.service.impl;
import com.atguigu.springcloud.service.IMessageProvider;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import javax.annotation.Resource;
import java.util.UUID;
@EnableBinding(Source.class)
public class MessageProviderImpl implements IMessageProvider {
    @Resource
    private MessageChannel output;
    @Override
    public String send() {
        String serial = UUID.randomUUID().toString();
        return this.send(serial);
    }
    @Override
    public String send(String msg) {
        System.out.println("*******:"+msg);
        return output.send(MessageBuilder.withPayload(msg).build())+"";
    }


}
