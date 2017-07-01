package ru.afilippo.messenger.commands;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.afilippo.messenger.messages.LoginMessage;
import ru.afilippo.messenger.messages.Message;
import ru.afilippo.messenger.messages.StatusMessage;
import ru.afilippo.messenger.net.ProtocolException;
import ru.afilippo.messenger.net.Session;
import ru.afilippo.messenger.store.User;
import ru.afilippo.messenger.store.UserStore;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;


public class RegisterCommand extends Command {

    private static Logger LOGGER = LogManager.getLogger(RegisterCommand.class.getName());

    private UserStore userStore;

    public RegisterCommand(UserStore userStore) {
        this.userStore = userStore;
    }

    @Override
    public void execute(Session session, Message message, BlockingQueue<Session> sessions) {

        try {
            LoginMessage loginMessage = (LoginMessage) message;

            User tmpUser = new User(-1L, loginMessage.getLogin(), loginMessage.getPassword());
            tmpUser = userStore.addUser(tmpUser);
            session.setUser(tmpUser);

            session.send(new LoginMessage(tmpUser));
        } catch (ProtocolException | IOException e) {
            LOGGER.error("Can't login user: ", e);
            try {
                session.send(new StatusMessage("Can't register user with this login and password"));
            } catch (ProtocolException | IOException e1) {
                LOGGER.error("Can't send message: ", e);
            }
        }

    }
}
