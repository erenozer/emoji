package com.emojibot.commands.utils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import io.github.cdimascio.dotenv.Dotenv;


public class MongoManager {
      private static final Dotenv config = Dotenv.configure().load();
      private static MongoClient mongoClient;
      private static MongoDatabase database;

    public static void connect() {
        mongoClient = MongoClients.create(config.get("MONGO_URI"));
        database = mongoClient.getDatabase("emojiBot");
    }

    public static MongoDatabase getDatabase() {
        if (database == null) {
            connect();
        }
        return database;
    }

    public static void disconnect() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}