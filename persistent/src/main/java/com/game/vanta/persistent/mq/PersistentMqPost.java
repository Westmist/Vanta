package com.game.vanta.persistent.mq;

public class PersistentMqPost {

    private String collectName;

    private String id;

    private byte[] payload;

    public PersistentMqPost() {
    }

    public String getCollectName() {
        return collectName;
    }

    public void setCollectName(String collectName) {
        this.collectName = collectName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }
}
