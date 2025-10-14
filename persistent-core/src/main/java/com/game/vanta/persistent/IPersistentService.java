package com.game.vanta.persistent;

import com.game.vanta.persistent.dao.IPersistent;

public interface IPersistentService {

  <T extends IPersistent> T find(Class<T> clazz, String id);

  <T extends IPersistent> void upsertAsync(T data);

  <T extends IPersistent> void upsertNow(T data);

  <T extends IPersistent> void remove(T data);
}
