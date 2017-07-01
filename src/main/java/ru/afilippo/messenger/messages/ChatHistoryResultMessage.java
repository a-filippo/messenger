package ru.afilippo.messenger.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.afilippo.messenger.store.MessageStore;



public class ChatHistoryResultMessage extends Message {
    @JsonProperty("messages")
    private MessageStore.TextMessageModel[] messages;

    public ChatHistoryResultMessage() {
        super(null, Type.MSG_CHAT_HISTORY_RESULT);
    }

    public ChatHistoryResultMessage(MessageStore.TextMessageModel[] messages) throws InstantiationException {
        super(null, Type.MSG_CHAT_HISTORY_RESULT);
        this.messages = messages;
    }

    public MessageStore.TextMessageModel[] getMessages(){
        return this.messages;
    }

    public void setMessages(MessageStore.TextMessageModel[] messages){
        this.messages = messages;
    }

    public String[] readAllMessages() {
        String[] out = new String[messages.length];
        int i = 0;
        for (MessageStore.TextMessageModel s : messages){
            out[i++] = s.toString();
        }
        return out;
    }
}
