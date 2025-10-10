package com.game.vanta.dao;

import com.game.vanta.persistent.dao.IRolePersistent;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@Document(collection = "role")
public class Role implements IRolePersistent {
    /**
     * 角色id
     */
    @Id
    private String id;

    private boolean dirty;

    private String name;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
