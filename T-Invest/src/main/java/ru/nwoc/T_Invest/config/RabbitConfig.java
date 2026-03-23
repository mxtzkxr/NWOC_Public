package ru.nwoc.T_Invest.config;

import jakarta.annotation.PostConstruct;
import lombok.Setter;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Setter
public class RabbitConfig {
    @Value("${queue.name}")
    private String queueName;

    @Bean
    public Queue queue(){
        return new Queue(queueName,true);
    }

    @Bean
    public CachingConnectionFactory connectionFactory(@Value("${spring.rabbitmq.host}") String host,
                                                      @Value("${spring.rabbitmq.username}") String username,
                                                      @Value("${spring.rabbitmq.password}") String password){
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(host);
        cachingConnectionFactory.setUsername(username);
        cachingConnectionFactory.setPassword(password);
        return cachingConnectionFactory;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(CachingConnectionFactory connectionFactory){
        return new RabbitAdmin(connectionFactory);
    }
}
