package ru.afilippo.messenger.commands;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.afilippo.messenger.messages.ChatHistoryMessage;
import ru.afilippo.messenger.messages.ChatHistoryResultMessage;
import ru.afilippo.messenger.messages.Message;
import ru.afilippo.messenger.messages.StatusMessage;
import ru.afilippo.messenger.net.ProtocolException;
import ru.afilippo.messenger.net.Session;
import ru.afilippo.messenger.store.MessageStore;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.BlockingQueue;


public class ChatHistoryCommand extends Command {
    private static Logger LOGGER = LogManager.getLogger(ChatHistoryCommand.class.getName());

    private MessageStore messageStore;

    public ChatHistoryCommand(MessageStore messageStore) {
        this.messageStore = messageStore;
    }

    @Override
    public void execute(Session session, Message message, BlockingQueue<Session> sessions){
        if (!session.isLogged()) return;

        try {
            ChatHistoryMessage chatHistoryMessage = (ChatHistoryMessage) message;

            Long chatId = chatHistoryMessage.getChatId();
            MessageStore.TextMessageModel[] historyMessages = messageStore.getHistoryMessagesByChatId(chatId);

            ChatHistoryResultMessage chatHistoryResultMessage = new ChatHistoryResultMessage(historyMessages);

            session.send(chatHistoryResultMessage);
        } catch (ClassCastException e) {
            LOGGER.error("Can't cast message: ", e);
        } catch (InstantiationException | ProtocolException | IOException e) {
            LOGGER.error("Can't get history: ", e);
            try {
                session.send(new StatusMessage("Can't find history for your chat"));
            } catch (ProtocolException | IOException e1) {
                LOGGER.error("Can't send message: ", e);
            }
        }


    }
}
