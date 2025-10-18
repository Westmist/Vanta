package com.game.vanta.redis.eventbus.pubsub;

public class RedisEventWrapper {

    private String className;

    private byte[] body;

    public RedisEventWrapper() {
    }

    public RedisEventWrapper(String className, byte[] body) {
        this.className = className;
        this.body = body;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

}
