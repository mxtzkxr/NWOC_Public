package ru.nwoc.T_Invest.service;

import lombok.Setter;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Setter
public class MessageSenderImpl {
    @Value("${queue.name}")
    private String queueName;
    @Autowired
    private AmqpTemplate amqpTemplate;

    public void sendMessage(String message){
        amqpTemplate.convertAndSend(queueName,message);

    }
}
