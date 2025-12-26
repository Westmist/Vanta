package org.markeb.meta.luban;

/**
 * Luban Bean 基础接口
 * 所有 Luban 生成的配置类都需要实现此接口
 */
public interface IBean {

    /**
     * 从二进制缓冲区反序列化
     *
     * @param buf 二进制缓冲区
     */
    void deserialize(ByteBuf buf);
}

