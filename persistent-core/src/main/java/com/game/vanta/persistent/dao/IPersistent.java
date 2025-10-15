package com.game.vanta.persistent.dao;

import java.time.Duration;

public interface IPersistent {

    String getId();

    void setId(String id);

    boolean isDirty();

    void setDirty(boolean dirty);

    Duration timeout();
}
