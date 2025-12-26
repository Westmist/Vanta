package org.markeb.meta.luban;

/**
 * Luban Bean 抽象基类
 * 提供通用的反序列化支持
 */
public abstract class AbstractBean implements IBean {

    /**
     * 构造函数（用于手动创建）
     */
    protected AbstractBean() {
    }

    /**
     * 从 ByteBuf 反序列化构造
     *
     * @param buf 二进制缓冲区
     */
    protected AbstractBean(ByteBuf buf) {
        deserialize(buf);
    }
}

