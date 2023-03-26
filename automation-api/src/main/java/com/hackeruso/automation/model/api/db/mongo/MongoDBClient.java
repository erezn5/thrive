package com.hackeruso.automation.model.api.db.mongo;


import com.hackeruso.automation.conf.EnvConf;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.eq;
public class MongoDBClient {
    private final MongoClient mongoClient;
    private final String mongoConnectionString = EnvConf.getProperty("mongo.db.connection.string");

    public MongoDBClient() {
        mongoClient =  MongoClients.create(mongoConnectionString);
    }

    private MongoDatabase getMongoDataBase(String dataBase){
        return mongoClient.getDatabase(dataBase);
    }

    private MongoCollection<Document> getMongoCollection(String dataBase, String collection){
        return getMongoDataBase(dataBase).getCollection(collection);
    }

    public FindIterable<Document> getRunningLabs(String dataBase, String collection){
        MongoCollection<Document> documentsCollection = getMongoCollection(dataBase, collection);
        Bson equalComparison = eq("status", "Ready");
        return documentsCollection.find(equalComparison);
    }
}
