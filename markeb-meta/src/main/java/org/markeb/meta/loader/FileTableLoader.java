package org.markeb.meta.loader;

import org.markeb.meta.TableLoader;
import org.markeb.meta.luban.ByteBuf;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;

/**
 * 文件系统配置表加载器
 * 从本地文件系统加载 Luban 生成的二进制配置数据
 */
@Slf4j
public class FileTableLoader implements TableLoader {

    private final Path basePath;
    private final String suffix;

    /**
     * 创建文件加载器
     *
     * @param basePath 配置文件基础路径
     */
    public FileTableLoader(String basePath) {
        this(basePath, ".bytes");
    }

    /**
     * 创建文件加载器
     *
     * @param basePath 配置文件基础路径
     * @param suffix   文件后缀名
     */
    public FileTableLoader(String basePath, String suffix) {
        this.basePath = Paths.get(basePath);
        this.suffix = suffix;
    }

    @Override
    public ByteBuf load(String tableName) throws IOException {
        Path filePath = basePath.resolve(tableName + suffix);
        
        if (!Files.exists(filePath)) {
            throw new IOException("Config file not found: " + filePath);
        }
        
        byte[] bytes = Files.readAllBytes(filePath);
        log.debug("Loaded config file: {} ({} bytes)", filePath, bytes.length);
        return new ByteBuf(bytes);
    }

    /**
     * 获取基础路径
     *
     * @return 基础路径
     */
    public Path getBasePath() {
        return basePath;
    }
}

