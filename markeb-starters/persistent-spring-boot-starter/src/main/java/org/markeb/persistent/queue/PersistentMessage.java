package org.markeb.persistent.queue;

import lombok.Data;

import java.io.Serializable;

/**
 * 持久化消息
 */
@Data
public class PersistentMessage implements Serializable {

    /**
     * 消息类型
     */
    private MessageType type;

    /**
     * 实体类名
     */
    private String entityClass;

    /**
     * 实体ID
     */
    private String entityId;

    /**
     * 集合/表名
     */
    private String collection;

    /**
     * 实体数据（序列化后）
     */
    private byte[] payload;

    /**
     * 时间戳
     */
    private long timestamp;

    public PersistentMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    public PersistentMessage(MessageType type, String entityClass, String entityId) {
        this();
        this.type = type;
        this.entityClass = entityClass;
        this.entityId = entityId;
    }

    public PersistentMessage(MessageType type, String entityClass, String entityId, byte[] payload) {
        this(type, entityClass, entityId);
        this.payload = payload;
    }

    /**
     * 获取实体 ID（entityId 的别名）
     */
    public String getId() {
        return entityId;
    }

    /**
     * 生成缓存键
     */
    public String getCacheKey() {
        String prefix = collection != null ? collection : entityClass;
        return prefix + ":" + entityId;
    }

    /**
     * 消息类型
     */
    public enum MessageType {
        /**
         * 保存/更新
         */
        SAVE,

        /**
         * 删除
         */
        DELETE
    }
}
