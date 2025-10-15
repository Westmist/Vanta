package com.game.vanta.persistent;

public class PersistentMqNotice {

    private String collectName;

    private String id;

    /*
     * 后续可扩展，由生产者直接传输待持久化的对象过来，消费者直接反序列化后存储
     * 这样可以减少消费者查询 redis 的依赖与开销
     */
    private byte[] payload;

    public PersistentMqNotice() {
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

    @Override
    public String toString() {
        return "PersistentMqNotice{" + "collectName='" + collectName + '\'' + ", id='" + id + '\'' + '}';
    }
}
