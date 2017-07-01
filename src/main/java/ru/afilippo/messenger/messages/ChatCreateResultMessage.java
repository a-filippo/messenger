package ru.afilippo.messenger.messages;

import java.util.List;


public class ChatCreateResultMessage extends Message{

    private long createdChatId;

    public ChatCreateResultMessage() {
        super(null, Type.MSG_CHAT_CREATE_RESULT);
    }

    public ChatCreateResultMessage(long createdChatId){
        super(null, Type.MSG_CHAT_CREATE_RESULT);
        this.createdChatId = createdChatId;
    }

    public long getCreatedChatId(){
        return createdChatId;
    }

    public void setCreatedChatId(long createdChatId){
        this.createdChatId = createdChatId;
    }
}
