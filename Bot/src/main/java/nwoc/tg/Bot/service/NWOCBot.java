package nwoc.tg.Bot.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import nwoc.tg.Bot.config.BotConfig;
import nwoc.tg.Bot.mapper.CommandParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Setter
public class NWOCBot implements LongPollingSingleThreadUpdateConsumer {
    private final BotConfig botConfig;
    private final TelegramClient telegramClient;


    private final String botOwnerId;
    private final String tInvestHost;
    private final String tInvestPort;
    private final String tInvestPath;

    @Autowired
    public NWOCBot(BotConfig botConfig,@Value("${bot.owner.chat.id}") String botOwnerId, @Value("${spring.t-invest.host}") String tInvestHost,
                   @Value("${spring.t-invest.port}") String tInvestPort, @Value("${spring.t-invest.path}") String tInvestPath){
        this.botConfig = botConfig;
        this.botOwnerId = botOwnerId;
        this.telegramClient = new OkHttpTelegramClient(botConfig.getToken());
        this.tInvestHost = tInvestHost;
        this.tInvestPort = tInvestPort;
        this.tInvestPath = tInvestPath;
    }


    public String getBotUsername() {
        return botConfig.getBotName();
    }

    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void consume(List<Update> updates) {
        for (Update update:updates){
            consume(update);
        }
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()&&!update.getMessage().getText().equals("/bondYield RU000A10BBD8")) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (update.getMessage().getChatId().toString().equals(botOwnerId)){
                String messageText = update.getMessage().getText();
                System.out.println(messageText);
                CommandParser.ParsedCommand parsed = CommandParser.parse(messageText);

                if (parsed != null) {
                    switch (parsed.getCommand()) {
                        case "start":
                            sendMessage("Привет, что хотим сегодня?");
                            break;
                        case "bondYield":
                            String uid = parsed.getParam(0);
                            if (uid != null) {
                                WebClient webClient = WebClient.builder()
                                        .baseUrl(tInvestHost+":"+tInvestPort+tInvestPath)
                                        .build();
                                Mono<ResponseEntity<String>> response = webClient.post().contentType(MediaType.TEXT_PLAIN).bodyValue(uid).retrieve().toEntity(String.class);
                                response.subscribe(
                                        stringResponseEntity -> {
                                            if(!stringResponseEntity.getStatusCode().equals(HttpStatusCode.valueOf(200))){
                                                sendMessage("Что то пошло не так...");
                                            }
                                        }
                                );
                            } else {
                                sendMessage("С Вашим UID что то не так...");
                            }
                            break;
                        default:
                            sendMessage("Эта команда мне неизвестна...");
                    }
                } else {
                    log.info("Получено сообщение: " + messageText);
                }
            }
        }
    }

    public void sendMessage(String message){
        SendMessage sendMessage = new SendMessage(botOwnerId,message);
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

}
