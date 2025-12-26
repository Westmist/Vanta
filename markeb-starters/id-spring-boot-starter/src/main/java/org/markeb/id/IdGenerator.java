package org.markeb.id;

/**
 * 分布式 ID 生成器接口
 */
public interface IdGenerator {

    /**
     * 生成下一个唯一 ID
     *
     * @return 唯一 ID
     */
    long nextId();

    /**
     * 批量生成唯一 ID
     *
     * @param count 数量
     * @return ID 数组
     */
    default long[] nextIds(int count) {
        long[] ids = new long[count];
        for (int i = 0; i < count; i++) {
            ids[i] = nextId();
        }
        return ids;
    }

    /**
     * 生成字符串格式的 ID
     *
     * @return 字符串 ID
     */
    default String nextIdStr() {
        return String.valueOf(nextId());
    }

    /**
     * 获取当前 WorkerId
     *
     * @return WorkerId
     */
    int getWorkerId();
}

