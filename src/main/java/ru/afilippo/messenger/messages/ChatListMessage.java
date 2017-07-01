package ru.afilippo.messenger.messages;

import ru.afilippo.messenger.store.User;

public class ChatListMessage extends Message {

    public ChatListMessage() {
        super(null, Type.MSG_CHAT_LIST_RESULT);
    }

    public ChatListMessage(User creator) throws InstantiationException {
        super(creator, Type.MSG_CHAT_LIST);
    }
}
