package ru.afilippo.messenger.commands;

import ru.afilippo.messenger.messages.Message;
import ru.afilippo.messenger.net.Session;

import java.util.concurrent.BlockingQueue;

public abstract class Command {
    public abstract void execute(Session session, Message message, BlockingQueue<Session> sessions);
}
