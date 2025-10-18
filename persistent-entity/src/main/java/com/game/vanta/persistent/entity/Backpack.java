package com.game.vanta.persistent.entity;

import com.game.vanta.persistent.dao.IRolePersistent;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Repository
@Document(collection = "backpack")
public class Backpack implements IRolePersistent {

    @Id
    private String id;

    private Map<String, String> items = new HashMap<>();

    private boolean dirty;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getItems() {
        return items;
    }

    public void setItems(Map<String, String> items) {
        this.items = items;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    @Override
    public Duration timeout() {
        return Duration.ofMinutes(30);
    }

}
