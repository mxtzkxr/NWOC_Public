package nwoc.tg.Bot.config;

import lombok.extern.slf4j.Slf4j;
import nwoc.tg.Bot.service.NWOCBot;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

@Slf4j
@Component
public class BotInitializer {

    @Bean(destroyMethod = "close")
    public TelegramBotsLongPollingApplication telegramBotsApplication(NWOCBot nwocBot) {
        try {
            TelegramBotsLongPollingApplication application =
                    new TelegramBotsLongPollingApplication();
            application.registerBot(nwocBot.getBotToken(), nwocBot);
            log.info("Telegram bot registered successfully");
            return application;
        } catch (Exception e) {
            throw new BeanCreationException("Failed to create Telegram bot", e);
        }
    }
}
