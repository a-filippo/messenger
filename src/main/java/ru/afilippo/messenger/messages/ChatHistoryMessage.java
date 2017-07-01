package ru.afilippo.messenger.messages;


import ru.afilippo.messenger.store.User;


public class ChatHistoryMessage extends Message {

    public ChatHistoryMessage() {
        super(null, Type.MSG_CHAT_HISTORY);
    }

    public ChatHistoryMessage(User creator, Long chatId) throws InstantiationException {
        super(creator, Type.MSG_CHAT_HISTORY);
        super.setChatId(chatId);
    }

    public Long getChatId() {
        return super.getChatId();
    }
}
