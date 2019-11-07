package jhu.group6.sounDJam.repositories;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import jhu.group6.sounDJam.utils.CollectionNames;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

public class MongoRepository {
    private static MongoClient mongoClient;
    private static MongoDatabase db;

    public MongoRepository(MongoClient mongoClient, String dbName) {
        this.mongoClient = mongoClient;
        this.db = this.mongoClient.getDatabase(dbName);

        for (CollectionNames collectionName : CollectionNames.values()) {
            var collection = db.getCollection(collectionName.toString());
            collection.createIndex(Indexes.hashed("sessionId"));
        }
    }

    public void insertIntoCollection(CollectionNames collection, Document document) {
        MongoCollection c = db.getCollection(collection.toString());
        c.insertOne(document);
    }

    public Document findOneFromCollection(CollectionNames collection, Document document) {
        MongoCollection c = db.getCollection(collection.toString());
        return (Document) c.find(document).first();
    }

    public List<Document> findAllFromCollection(CollectionNames collection, Document document) {
        MongoCollection c = db.getCollection(collection.toString());
        return (List<Document>) c.find(document).into(new ArrayList<Document>());
    }

    public Document findOneFromCollectionBySessionId(CollectionNames collection, String id) {
        Document searchParams = new Document("sessionId", id);
        return findOneFromCollection(collection, searchParams);
    }

    public Document findOneFromCollectionBySessionId(CollectionNames collection, UUID id) {
        return findOneFromCollectionBySessionId(collection, id.toString());
    }

    public List<Document> findAllFromCollectionBySessionId(CollectionNames collection, String id) {
        Document searchParams = new Document("sessionId", id);
        return findAllFromCollection(collection, searchParams);
    }

    public List<Document> findAllFromCollectionBySessionId(CollectionNames collection, UUID id) {
        return findAllFromCollectionBySessionId(collection, id.toString());
    }

    public long removeOneFromCollectionById(CollectionNames collection, String id) {
        MongoCollection c = db.getCollection(collection.toString());
        return c.deleteOne(eq(collection.toString().toLowerCase() + "Id", id)).getDeletedCount();
    }

    public long removeOneFromCollectionById(CollectionNames collection, UUID id) {
        return removeOneFromCollectionById(collection, id.toString());
    }

    public long removeOneFromCollectionBySessionId(CollectionNames collection, String id) {
        MongoCollection c = db.getCollection(collection.toString());
        return c.deleteOne(eq("sessionId", id)).getDeletedCount();
    }

    public long removeOneFromCollectionBySessionId(CollectionNames collection, UUID id) {
        return removeOneFromCollectionBySessionId(collection, id.toString());
    }

    public long removeAllFromCollectionById(CollectionNames collection, String id) {
        MongoCollection c = db.getCollection(collection.toString());
        return c.deleteMany(eq("sessionId", id)).getDeletedCount();
    }

    public long removeAllFromCollectionById(CollectionNames collection, UUID id) {
        return removeAllFromCollectionById(collection, id.toString());
    }

    public boolean updateOneFromCollectionBySessionId(CollectionNames collection, String id, Document updateDoc) {
        MongoCollection c = db.getCollection(collection.toString());
        return c.replaceOne(eq("sessionId", id), updateDoc).wasAcknowledged();
    }

    public boolean updateOneFromCollectionBySessionId(CollectionNames collection, UUID id, Document updateDoc) {
        return updateOneFromCollectionBySessionId(collection, id.toString(), updateDoc);
    }

    public boolean updateUser(Document user) {
        MongoCollection c = db.getCollection(CollectionNames.USER.toString());
        return c.replaceOne(eq("userId", user.get("userId")), user).wasAcknowledged();
    }

    public long purgeAllBySessionId(String sessionId) {
        var deleteCount = 0;
        for (CollectionNames collectionName : CollectionNames.values()) {
            var collection = db.getCollection(collectionName.toString());
            deleteCount += collection.deleteMany(eq("sessionId", sessionId)).getDeletedCount();
        }
        return deleteCount;
    }

    public void dropCollection(String collection) {
        MongoCollection c = db.getCollection(collection);
        c.drop();
    }
}
