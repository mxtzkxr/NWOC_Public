package ru.nwoc.T_Invest.config;

import jakarta.annotation.PostConstruct;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Component;

@Component
public class RabbitInitializer {

    private final RabbitAdmin rabbitAdmin;
    private final Queue queue;

    public RabbitInitializer(RabbitAdmin rabbitAdmin, Queue queue) {
        this.rabbitAdmin = rabbitAdmin;
        this.queue = queue;
    }

    @PostConstruct
    public void init() {
        rabbitAdmin.declareQueue(queue);
    }
}