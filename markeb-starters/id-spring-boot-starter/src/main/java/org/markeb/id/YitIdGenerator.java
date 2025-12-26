package org.markeb.id;

import com.github.yitter.contract.IdGeneratorOptions;
import com.github.yitter.idgen.YitIdHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于 Yitter IdGenerator 的分布式 ID 生成器实现
 * 
 * 特点：
 * - 雪花漂移算法，高性能（50W/0.1s）
 * - ID 长度更短
 * - 支持时间回拨处理
 * 
 * @see <a href="https://github.com/yitter/IdGenerator">Yitter IdGenerator</a>
 */
public class YitIdGenerator implements IdGenerator {

    private static final Logger log = LoggerFactory.getLogger(YitIdGenerator.class);

    private final int workerId;

    /**
     * 创建 ID 生成器
     *
     * @param options 配置选项
     */
    public YitIdGenerator(IdGeneratorOptions options) {
        this.workerId = options.WorkerId;
        YitIdHelper.setIdGenerator(options);
        log.info("YitIdGenerator initialized with WorkerId: {}", workerId);
    }

    @Override
    public long nextId() {
        return YitIdHelper.nextId();
    }

    @Override
    public int getWorkerId() {
        return workerId;
    }

    /**
     * 解析 ID 中的时间戳
     *
     * @param id ID
     * @return 时间戳（毫秒）
     */
    public static long parseTime(long id) {
        // 根据默认配置解析
        // ID 结构: 1位符号 + 时间戳 + WorkerId + 序列号
        return id >> 22; // 默认 WorkerIdBitLength=6, SeqBitLength=6
    }

    /**
     * 解析 ID 中的 WorkerId
     *
     * @param id                  ID
     * @param workerIdBitLength   WorkerId 位长度
     * @param seqBitLength        序列号位长度
     * @return WorkerId
     */
    public static int parseWorkerId(long id, int workerIdBitLength, int seqBitLength) {
        return (int) ((id >> seqBitLength) & ((1 << workerIdBitLength) - 1));
    }
}

