package org.markeb.common.cons;

public class ServerConstant {

    enum ServerType {
        GATEWAY(1, "网关服"),
        NODE(2, "节点服"),
        MESH(3, "Mesh服"),
        ;

        private final int type;

        private final String desc;

        ServerType(int type, String desc) {
            this.type = type;
            this.desc = desc;
        }

        public int getType() {
            return type;
        }

        public String getDesc() {
            return desc;
        }
    }

}
