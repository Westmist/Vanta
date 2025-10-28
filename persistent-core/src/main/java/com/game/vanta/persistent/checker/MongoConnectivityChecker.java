package com.game.vanta.persistent.checker;

import com.game.vanta.persistent.checker.abs.IChecker;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;

public class MongoConnectivityChecker implements IChecker {

    private static final Logger log = LoggerFactory.getLogger(MongoConnectivityChecker.class);

    private final MongoClient mongoClient;

    private final MongoProperties mongoProperties;

    public MongoConnectivityChecker(MongoClient mongoClient, MongoProperties mongoProperties) {
        this.mongoClient = mongoClient;
        this.mongoProperties = mongoProperties;
    }

    @Override
    public String name() {
        return "MongoDB";
    }

    @Override
    public void check() {
        String mongoUri = mongoProperties.getUri();
        String dbName = mongoProperties.getMongoClientDatabase();
        log.info("Checking MongoDB: {}, database: {}", mongoUri, dbName);
        MongoDatabase db = mongoClient.getDatabase(dbName);
        Document result = db.runCommand(new Document("ping", 1));
        Object ok = result.get("ok");
        if (!(ok instanceof Number) || ((Number) ok).intValue() != 1) {
            throw new IllegalStateException("Unexpected MongoDB ping response: " + result.toJson());
        }
        log.info("MongoDB ping response: {}", result.toJson());
    }


}
