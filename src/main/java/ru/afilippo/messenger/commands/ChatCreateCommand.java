package ru.afilippo.messenger.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.afilippo.messenger.messages.ChatCreateMessage;
import ru.afilippo.messenger.messages.ChatCreateResultMessage;
import ru.afilippo.messenger.messages.Message;
import ru.afilippo.messenger.net.ProtocolException;
import ru.afilippo.messenger.net.Session;
import ru.afilippo.messenger.store.MessageStore;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;


public class ChatCreateCommand extends Command {

    private static Logger LOGGER = LogManager.getLogger(ChatCreateCommand.class.getName());
    private MessageStore messageStore;

    public ChatCreateCommand(MessageStore messageStore) {
        this.messageStore = messageStore;
    }

    @Override
    public void execute(Session session, Message message, BlockingQueue<Session> sessions) {
        if (!session.isLogged()) return;

        try {
            ChatCreateMessage chatCreateMessage = (ChatCreateMessage) message;

            Set<Long> users = new TreeSet<>(chatCreateMessage.getParticipants());
            users.add(chatCreateMessage.getSenderId());

            Long[] allParticipiants = users.toArray(new Long[users.size()]);

            long created = messageStore.createChat(allParticipiants);

            ChatCreateResultMessage resultMessage = new ChatCreateResultMessage(created);
            session.send(resultMessage);
        } catch (ClassCastException e) {
            LOGGER.error("Can't cast message: ", e);
        } catch (ProtocolException | IOException e){
            LOGGER.error("Can't create chat: ", e);
        }
    }
}

