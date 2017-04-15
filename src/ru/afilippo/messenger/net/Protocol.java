package ru.afilippo.messenger.net;

import ru.afilippo.messenger.messages.Message;

/**
 *
 */
public interface Protocol {

    Message decode(byte[] bytes) throws ProtocolException;

    byte[] encode(Message msg) throws ProtocolException;

}
