package nwoc.tg.Bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RabbitReceiver {
    private final NWOCBot nwocBot;

    @RabbitListener(queues = {"InvestInfo"})
    public void receive(String message) {
        log.info("received message : " + message);
        nwocBot.sendMessage(message);
    }
}
