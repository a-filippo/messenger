package ru.afilippo.messenger.messages;


public class StatusMessage extends Message {
    private String info;

    public StatusMessage() {
    }

    public StatusMessage(String info){
        super(null, Type.MSG_STATUS);
        this.info = info;
    }

    public String getInfo() {
        return info;
    }
}
