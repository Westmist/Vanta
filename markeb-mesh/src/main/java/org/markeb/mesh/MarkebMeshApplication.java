package org.markeb.mesh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *
 * 职责：
 * 1. 匹配服务（Matchmaking）
 * 2. 大厅服务（Lobby）
 * 3. 排行榜服务
 * 4. 公告服务
 * 5. 其他无状态服务
 */
@SpringBootApplication
public class MarkebMeshApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarkebMeshApplication.class, args);
    }

}

