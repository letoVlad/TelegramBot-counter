package com.example.telegram_counter.counter;

import com.example.telegram_counter.components.BotCommands;
import com.example.telegram_counter.config.TelegramBotConfiguration;
//import com.example.telegram_counter.repository.UserRepository;
import com.example.telegram_counter.entity.Message;
import com.example.telegram_counter.entity.User;
import com.example.telegram_counter.entity.UserHQL;
import com.querydsl.core.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class TelegramBotCounter extends TelegramLongPollingBot implements BotCommands {
    private final TelegramBotConfiguration telegramBotConfiguration;
    private final UserHQL userHQL;

    public TelegramBotCounter(TelegramBotConfiguration telegramBotConfiguration, UserHQL userHQL) {
        this.telegramBotConfiguration = telegramBotConfiguration;
        this.userHQL = userHQL;
        try {
            this.execute(new SetMyCommands(LIST_OF_COMMANDS, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return telegramBotConfiguration.getBotName();
    }

    @Override
    public String getBotToken() {
        return telegramBotConfiguration.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        long chatId = 0;
        long userId = 0;
        String userName = null;
        String receivedMessage;


        //если получено сообщение текстом
        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
            userId = update.getMessage().getFrom().getId();
            if (update.getMessage().getFrom().getUserName() == null) {
                userName = update.getMessage().getFrom().getFirstName();
            } else {
                userName = update.getMessage().getFrom().getUserName();
            }

            if (update.getMessage().hasText()) {
                receivedMessage = update.getMessage().getText();
                botAnswerUtils(receivedMessage, chatId, userName);
            } else if (update.hasCallbackQuery()) {
                chatId = update.getCallbackQuery().getMessage().getChatId();
                userId = update.getCallbackQuery().getFrom().getId();
                userName = update.getCallbackQuery().getFrom().getUserName();
                receivedMessage = update.getCallbackQuery().getData();
                botAnswerUtils(receivedMessage, chatId, userName);
            }
            if (chatId == -874446740) {
                updateDB(userId, userName);
            } else if (chatId == -969629966) {
                updateDB(userId, userName);
            }
        }
    }

    private void botAnswerUtils(String receivedMessage, long chatId, String userName) {
        switch (receivedMessage) {
            case "/start" -> startBot(chatId, userName);
            case "/help" -> sendHelpText(chatId);
            case "кто больше всех говорит за день!?" -> ListPeopleMostPosts(chatId);
            case "кто больше всех говорит за неделю!?" -> ListPeopleMostPostsWeek(chatId);
            default -> {
            }
        }
    }

    //получаем список имя/количество сообщений и распарсиваем его для вывода(ЗА ДЕНЬ)
    private void ListPeopleMostPosts(long chatId) {
        List<Tuple> tuples = userHQL.topUserMostPostsPerDay();
        StringBuilder sb = new StringBuilder();

        for (Tuple tuple : tuples) {
            String name = tuple.get(0, String.class);
            int msgNumber = tuple.get(1, Integer.class);
            sb.append(name).append(" - ").append(msgNumber).append("\n");
        }
        String result = sb.toString();
        SendMessage message = new SendMessage(String.valueOf(chatId), result);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    //получаем список имя/количество сообщений и распарсиваем его для вывода (ЗА Неделю)
    private void ListPeopleMostPostsWeek(long chatId) {
        List<Tuple> tuples = userHQL.topUserMostPostsPerWeek();
        StringBuilder sb = new StringBuilder();

        for (Tuple tuple : tuples) {
            String name = tuple.get(0, String.class);
            int msgNumber = tuple.get(1, Integer.class);
            sb.append(name).append(" - ").append(msgNumber).append("\n");
        }
        String result = sb.toString();
        SendMessage message = new SendMessage(String.valueOf(chatId), result);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendHelpText(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(BotCommands.HELP_TEXT);

        try {
            execute(message);
            log.info("Reply sent");
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private void startBot(long chatId, String userName) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Дарова " + userName);

        try {
            execute(message);
            log.info("Reply sent");
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private void updateDB(long userId, String userName) {
        User user = new User();
        Message msg = new Message();
        if (!userHQL.findById(userId)) {
            msg.setLocalDateTime(LocalDateTime.now());
            msg.setUser(user);
            user.setId(userId);
            user.setName(userName);
            user.setMsg_numb(1);
            userHQL.saveUser(user);
            userHQL.saveMSG(msg);
        } else {
            msg.setLocalDateTime(LocalDateTime.now());
            msg.setUser(userHQL.findByUser(userId));
            userHQL.saveMSG(msg);
            userHQL.updateMsgNumberByUserId(userId);
        }
    }
}

