package org.markeb.meta.loader;

import org.markeb.meta.TableLoader;
import org.markeb.meta.luban.ByteBuf;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

/**
 * Classpath 配置表加载器
 * 从类路径加载 Luban 生成的二进制配置数据
 */
@Slf4j
public class ClasspathTableLoader implements TableLoader {

    private final String basePath;
    private final String suffix;
    private final ClassLoader classLoader;

    /**
     * 创建 Classpath 加载器
     *
     * @param basePath 配置文件基础路径（相对于 classpath）
     */
    public ClasspathTableLoader(String basePath) {
        this(basePath, ".bytes", Thread.currentThread().getContextClassLoader());
    }

    /**
     * 创建 Classpath 加载器
     *
     * @param basePath 配置文件基础路径（相对于 classpath）
     * @param suffix   文件后缀名
     */
    public ClasspathTableLoader(String basePath, String suffix) {
        this(basePath, suffix, Thread.currentThread().getContextClassLoader());
    }

    /**
     * 创建 Classpath 加载器
     *
     * @param basePath    配置文件基础路径（相对于 classpath）
     * @param suffix      文件后缀名
     * @param classLoader 类加载器
     */
    public ClasspathTableLoader(String basePath, String suffix, ClassLoader classLoader) {
        this.basePath = normalizePath(basePath);
        this.suffix = suffix;
        this.classLoader = classLoader != null ? classLoader : ClasspathTableLoader.class.getClassLoader();
    }

    @Override
    public ByteBuf load(String tableName) throws IOException {
        String resourcePath = basePath + tableName + suffix;
        
        try (InputStream is = classLoader.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Config resource not found: " + resourcePath);
            }
            
            byte[] bytes = is.readAllBytes();
            log.debug("Loaded config resource: {} ({} bytes)", resourcePath, bytes.length);
            return new ByteBuf(bytes);
        }
    }

    private String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        // 确保路径以 / 结尾
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        // 移除开头的 /
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }

    /**
     * 获取基础路径
     *
     * @return 基础路径
     */
    public String getBasePath() {
        return basePath;
    }
}

