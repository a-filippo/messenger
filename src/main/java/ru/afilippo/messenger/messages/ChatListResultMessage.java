package ru.afilippo.messenger.messages;


import java.util.List;


public class ChatListResultMessage extends Message {
    private List<Long> chats;

    public ChatListResultMessage() {
        super(null, Type.MSG_CHAT_LIST_RESULT);
    }

    public ChatListResultMessage(List<Long> chats) {
        super(null, Type.MSG_CHAT_LIST_RESULT);
        this.chats = chats;
    }

    public List<Long> getChats() {
        return chats;
    }
}
