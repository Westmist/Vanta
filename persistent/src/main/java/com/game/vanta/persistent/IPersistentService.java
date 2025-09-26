package com.game.vanta.persistent;

import com.game.vanta.persistent.dao.IPersistent;

public interface IPersistentService {

    <T extends IPersistent> T find(Class<T> clazz, String id);

    <T extends IPersistent> T updateAsync(T data);

    <T extends IPersistent> T saveNow(T data);

    <T extends IPersistent> void remove(T data);

}
