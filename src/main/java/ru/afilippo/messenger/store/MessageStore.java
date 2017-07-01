package ru.afilippo.messenger.store;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.afilippo.messenger.messages.Message;

import java.util.List;

public interface MessageStore {
    /**
     * получаем список ид пользователей заданного чата
     */
    List<Long> getChatsByUserId(Long userId);

    /**
     * получить информацию о чате
     */
    //Chat getChatById(Long chatId);

    /**
     * Список сообщений из чата
     */
    List<Long> getMessagesFromChat(Long chatId);

    /**
     * Получить информацию о сообщении
     */
    Message getMessageById(Long messageId);

    /**
     * Добавить сообщение в чат
     */
    void addMessage(Long chatId, Message message);

    /**
     * Создать чат
     */
    Long createChat(Long[] users);

    /**
     * Получить массив айди пользователей по айди чата
     */
    Long[] getUserIdsByChatId(long chatId);

    /**
     * Добавить пользователя к чату
     */
    void addUserToChat(Long userId, Long chatId);

    public TextMessageModel[] getHistoryMessagesByChatId(Long chatId);

    public class TextMessageModel{
        @JsonProperty("userId")
        private long userId;

        @JsonProperty("message")
        private String message;

        public TextMessageModel(long userId, String message){
            this.userId = userId;
            this.message = message;
        }

        public long getUserId(){
            return this.userId;
        }

        public void setUserId(long userId){
            this.userId = userId;
        }

        public String getMessage(){
            return this.message;
        }

        public void setMessage(String message){
            this.message = message;
        }

        @Override
        public String toString(){
            return "id: " + userId + ", text: " + message;
        }
    }
}
