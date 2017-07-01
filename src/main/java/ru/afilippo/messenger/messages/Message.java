package ru.afilippo.messenger.messages;

import ru.afilippo.messenger.store.User;

import java.io.Serializable;

public abstract class Message implements Serializable {
    private Long senderId;
    private Long chatId;
    private Type type;

    public Message(){

    }

    public Message(User sender, Long chatId, Type type) {
        if (sender != null)
            this.senderId = sender.getId();

        this.chatId = chatId;
        this.type = type;
    }

    public Message(Long senderId, Long chatId, Type type) {
        this.senderId = senderId;
        this.chatId = chatId;
        this.type = type;
    }

    public Message(User sender, Type type) {
        if (sender != null)
            this.senderId = sender.getId();

        this.type = type;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Message{" +
                "senderId=" + senderId +
                ", chatId=" + chatId +
                ", type=" + type +
                '}';
    }
}
