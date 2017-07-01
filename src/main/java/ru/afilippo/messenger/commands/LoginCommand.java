package ru.afilippo.messenger.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.afilippo.messenger.messages.LoginMessage;
import ru.afilippo.messenger.messages.Message;
import ru.afilippo.messenger.net.ProtocolException;
import ru.afilippo.messenger.net.Session;
import ru.afilippo.messenger.store.User;
import ru.afilippo.messenger.store.UserStore;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;


public class LoginCommand extends Command {

    private static Logger LOGGER = LogManager.getLogger(LoginCommand.class.getName());

    private UserStore userStore;

    public LoginCommand(UserStore userStore) {
        this.userStore = userStore;
    }

    @Override
    public void execute(Session session, Message message, BlockingQueue<Session> sessions) {

        try {
            LoginMessage loginMessage = (LoginMessage) message;
            User user = userStore.getUser(loginMessage.getLogin(), loginMessage.getPassword());
            session.setUser(user);
            loginMessage.setSenderId((user != null) ? user.getId() : null);

            session.send(loginMessage);

        } catch (ProtocolException | IOException e) {
            LOGGER.error("Can't manage this command: ", e);
        }
    }
}
