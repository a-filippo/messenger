package ru.afilippo.messenger.net;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.afilippo.messenger.messages.*;


import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringProtocol implements Protocol {

    private ObjectMapper mapper = new ObjectMapper();
    private static Logger LOGGER = LogManager.getLogger(StringProtocol.class.getName());
    private Pattern pattern = Pattern.compile("^([a-zA-Z0-9_]+)?((.|\\s)+)$");

    @Override
    public Message decode(byte[] bytes) throws ProtocolException {
        String s = new String(bytes);
        Matcher m = pattern.matcher(s);

        if (!m.find()){
            throw new ProtocolException("Can not parse pattern");
        }

        Type messageType = Type.valueOf(m.group(1));
        String jsonMessage = m.group(2);


        Message decoded;
        switch (messageType){
            case MSG_TEXT:
                decoded = decodeMessage(jsonMessage, TextMessage.class);
                break;
            case MSG_LOGIN:
            case MSG_REGISTER:
                decoded = decodeMessage(jsonMessage, LoginMessage.class);
                break;
            case MSG_CHAT_CREATE:
                decoded = decodeMessage(jsonMessage, ChatCreateMessage.class);
                break;
            case MSG_INFO_RESULT:
            case MSG_STATUS:
                decoded = decodeMessage(jsonMessage, StatusMessage.class);
                break;
            case MSG_INFO:
                decoded = decodeMessage(jsonMessage, UserInfoMessage.class);
                break;
            case MSG_CHAT_CREATE_RESULT:
                decoded = decodeMessage(jsonMessage, ChatCreateResultMessage.class);
                break;
            case MSG_CHAT_LIST:
                decoded = decodeMessage(jsonMessage, ChatListMessage.class);
                break;
            case MSG_CHAT_LIST_RESULT:
                decoded = decodeMessage(jsonMessage, ChatListResultMessage.class);
                break;
            case MSG_CHAT_HISTORY:
                decoded = decodeMessage(jsonMessage, ChatHistoryMessage.class);
                break;
            case MSG_CHAT_HISTORY_RESULT:
                decoded = decodeMessage(jsonMessage, ChatListResultMessage.class);
                break;
            default:
                throw new ProtocolException("Invalid type: " + messageType);
        }

        return decoded;
    }

    private <T> T decodeMessage(String jsonMessage, Class<T> msgClass) throws ProtocolException{
        try {
            return mapper.readValue(jsonMessage, msgClass);
        } catch (IOException e) {
            LOGGER.error("Invalid type", e);
            throw new ProtocolException("Cannot deserialize ChatHistoryResultMessage from json: "
                    + jsonMessage + " " + e.getMessage());
        }
    }

    @Override
    public byte[] encode(Message msg) throws ProtocolException {
        StringBuilder encodedString = new StringBuilder();

        try {
            encodedString.append(mapper.writeValueAsString(msg));
        } catch (JsonProcessingException e) {
            LOGGER.error("Serialization problems: ", e);
            throw new ProtocolException("Cant serialize TextMessage");
        } catch (ClassCastException classCastException) {
            LOGGER.error("Class cast problems: ", classCastException);
            throw new ProtocolException("Can't cast Message to TextMessage");
        } catch (IOException e){
            e.printStackTrace();
        }


        LOGGER.log(Level.INFO, "Encoded: " + encodedString);
        String out = msg.getType() + encodedString.toString();
        return out.getBytes();
    }
}