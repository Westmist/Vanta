package com.game.vanta.persistent;

import com.google.common.base.Joiner;

public class PersistentUtil {

    private static final Joiner JOINER = Joiner.on(":").skipNulls();

    public static String build(String collectName, String id) {
        return JOINER.join(collectName, id);
    }

}
