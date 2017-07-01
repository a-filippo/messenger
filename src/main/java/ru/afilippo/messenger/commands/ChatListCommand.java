package ru.afilippo.messenger.commands;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.afilippo.messenger.messages.ChatListMessage;
import ru.afilippo.messenger.messages.ChatListResultMessage;
import ru.afilippo.messenger.messages.Message;
import ru.afilippo.messenger.messages.StatusMessage;
import ru.afilippo.messenger.net.ProtocolException;
import ru.afilippo.messenger.net.Session;
import ru.afilippo.messenger.store.MessageStore;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ChatListCommand extends Command {

    private static Logger LOGGER = LogManager.getLogger(ChatListCommand.class.getName());
    private MessageStore messageStore;

    public ChatListCommand(MessageStore messageStore) {
        this.messageStore = messageStore;
    }

    @Override
    public void execute(Session session, Message message, BlockingQueue<Session> sessions) {
        if (!session.isLogged()) return;

        try {
            ChatListMessage chatListMessage = (ChatListMessage) message;
            List<Long> chats = messageStore.getChatsByUserId(chatListMessage.getSenderId());
            Message chatList = new ChatListResultMessage(chats);

            session.send(chatList);

        } catch (ProtocolException | IOException e) {
            LOGGER.error("Can't chat ids: ", e);
            try {
                session.send(new StatusMessage("Can't get chats for you."));
            } catch (ProtocolException | IOException e1) {
                LOGGER.error("Can't send message: ", e);
            }
        } catch (ClassCastException e) {
            LOGGER.error("Can't cast message: ", e);
        }

    }
}
