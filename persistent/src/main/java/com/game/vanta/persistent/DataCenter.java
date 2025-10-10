package com.game.vanta.persistent;

import com.game.vanta.persistent.dao.IPersistent;
import com.game.vanta.persistent.mq.DefaultPersistentMqSendCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataCenter {

    private static final Logger log = LoggerFactory.getLogger(DataCenter.class);

    public static IPersistentService service;

    @Autowired
    public void setInstance(IPersistentService service) {
        DataCenter.service = service;
        log.info("DataCenter instance initialized.");
    }

    public <T extends IPersistent> T find(Class<T> clazz, String id) {
        return service.find(clazz, id);
    }

    public <T extends IPersistent> void upsertAsync(T data) {
        service.upsertAsync(data);
    }

    public <T extends IPersistent> void upsertNow(T data) {
        service.upsertNow(data);
    }

    public <T extends IPersistent> void remove(T data) {
        service.remove(data);
    }

}
