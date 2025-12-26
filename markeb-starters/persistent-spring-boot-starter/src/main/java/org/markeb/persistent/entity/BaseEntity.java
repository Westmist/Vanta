package org.markeb.persistent.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类（可选继承）
 * 提供通用字段
 */
@Data
public abstract class BaseEntity<ID> implements Identifiable<ID>, Serializable {

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 版本号（用于乐观锁）
     */
    private Long version;
}

