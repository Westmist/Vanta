package org.markeb.meta;

import org.markeb.meta.luban.ByteBuf;

import java.io.IOException;

/**
 * 配置表数据加载器接口
 * 支持不同的数据源（本地文件、远程服务等）
 */
@FunctionalInterface
public interface TableLoader {

    /**
     * 加载指定表的二进制数据
     *
     * @param tableName 表名
     * @return 二进制缓冲区
     * @throws IOException 加载失败时抛出
     */
    ByteBuf load(String tableName) throws IOException;
}

