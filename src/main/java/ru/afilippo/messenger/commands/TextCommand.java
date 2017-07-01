package ru.afilippo.messenger.commands;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.afilippo.messenger.messages.Message;
import ru.afilippo.messenger.messages.StatusMessage;
import ru.afilippo.messenger.messages.TextMessage;
import ru.afilippo.messenger.net.ProtocolException;
import ru.afilippo.messenger.net.Session;
import ru.afilippo.messenger.net.ThreadPool;
import ru.afilippo.messenger.store.MessageStore;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;


public class TextCommand extends Command {

    private static Logger LOGGER = LogManager.getLogger(TextCommand.class.getName());
    private MessageStore messageStore;

    public TextCommand(MessageStore messageStore) {
        this.messageStore = messageStore;
    }


    @Override
    public void execute(Session session, Message message, BlockingQueue<Session> sessions) {
        if (!session.isLogged()) return;

        TextMessage textMessage = (TextMessage) message;

        try {
            messageStore.addMessage(textMessage.getChatId(), textMessage);

            Long[] receiverIds = messageStore.getUserIdsByChatId(message.getChatId());

            ThreadPool.addWork(() -> {
                for (Session participantSession : sessions) {
                    for (long userId : receiverIds) {
                        if (participantSession.getUser() == null) continue;

                        long getter = participantSession.getUser().getId();

                        if (getter != userId || message.getSenderId() == getter) continue;

                        if (participantSession.getUser() != null && participantSession.getUser().getId() == userId) {
                            try {
                                participantSession.send(textMessage);
                            } catch (ProtocolException | IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });

            StatusMessage statusMessage = new StatusMessage("Your message is delivered to chat "
                    + message.getChatId());

            session.send(statusMessage);
        } catch (ProtocolException | IOException e) {
            LOGGER.error("Can't send message: ", e);
        }
    }
}
