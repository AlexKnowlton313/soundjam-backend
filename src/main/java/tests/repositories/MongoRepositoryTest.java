package tests.repositories;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import jhu.group6.sounDJam.models.User;
import jhu.group6.sounDJam.repositories.MongoRepository;
import jhu.group6.sounDJam.utils.CollectionNames;
import org.bson.Document;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

/**
 * To run these tests, ensure mongoDB is running locally on port 27017:
 *
 *  mongod --config /usr/local/etc/mongod.conf --fork
 *
 */

public class MongoRepositoryTest {
    private static String DATABASE_NAME = "test";
    private static MongoRepository repository;
    private static Document test1 = new Document("name", "test1")
            .append("sessionId", "7dc53df5-703e-49b3-8670-b1c468f47f1f");
    private static Document test2 = new Document("name", "test2")
            .append("sessionId", "7dc53df5-703e-49b3-8670-b1c468f47f1f");

    @Before
    public void createRepository() {
        MongoClient mongoClient = MongoClients.create();
        repository = new MongoRepository(mongoClient, DATABASE_NAME);

        repository.insertIntoCollection(CollectionNames.MONGO_TESTING, test1);
        repository.insertIntoCollection(CollectionNames.MONGO_TESTING, test2);
    }

    @Test
    public void insertIntoCollectionTest() {
        Document doc = repository.findOneFromCollection(CollectionNames.MONGO_TESTING, test1);
        assertEquals(doc.get("name"), test1.get("name"));
        assertEquals(doc.get("sessionId"), test1.get("sessionId"));
    }

    @Test
    public void findAllFromCollectionTest() {
        Document searchParams = new Document("sessionId", "7dc53df5-703e-49b3-8670-b1c468f47f1f");
        List<Document> docs = repository.findAllFromCollection(CollectionNames.MONGO_TESTING, searchParams);

        assertEquals(docs.get(0).get("name"), test1.get("name"));
        assertEquals(docs.get(0).get("sessionId"), test1.get("sessionId"));
        assertEquals(docs.get(1).get("name"), test2.get("name"));
        assertEquals(docs.get(1).get("sessionId"), test2.get("sessionId"));
    }

    @Test
    public void findOneFromCollectionBySessionIdStringTest() {
        String id = test1.getString("sessionId");
        Document doc = repository.findOneFromCollectionBySessionId(CollectionNames.MONGO_TESTING, id);
        assertEquals(doc.get("name"), test1.get("name"));
        assertEquals(doc.get("sessionId"), test1.get("sessionId"));
    }

    @Test
    public void findOneFromCollectionBySessionIdUUIDTest() {
        UUID id = UUID.fromString(test1.getString("sessionId"));
        Document doc = repository.findOneFromCollectionBySessionId(CollectionNames.MONGO_TESTING, id);
        assertEquals(doc.get("name"), test1.get("name"));
        assertEquals(doc.get("sessionId"), test1.get("sessionId"));
    }

    @Test
    public void findAllFromCollectionBySessionIdStringTest() {
        String id = test1.getString("sessionId");
        List<Document> docs = repository.findAllFromCollectionBySessionId(CollectionNames.MONGO_TESTING, id);

        assertEquals(docs.size(), 2);

        assertEquals(docs.get(0).get("name"), test1.get("name"));
        assertEquals(docs.get(0).get("sessionId"), test1.get("sessionId"));
        assertEquals(docs.get(1).get("name"), test2.get("name"));
        assertEquals(docs.get(1).get("sessionId"), test2.get("sessionId"));
    }

    @Test
    public void findAllFromCollectionBySessionIdUUIDTest() {
        UUID id = UUID.fromString(test1.getString("sessionId"));
        List<Document> docs = repository.findAllFromCollectionBySessionId(CollectionNames.MONGO_TESTING, id);

        assertEquals(docs.size(), 2);

        assertEquals(docs.get(0).get("name"), test1.get("name"));
        assertEquals(docs.get(0).get("sessionId"), test1.get("sessionId"));
        assertEquals(docs.get(1).get("name"), test2.get("name"));
        assertEquals(docs.get(1).get("sessionId"), test2.get("sessionId"));
    }

    @Test
    public void removeOneFromCollectionByIdStringTest() {
        repository.insertIntoCollection(CollectionNames.SESSION, test1);

        String id = test1.getString("sessionId");
        long deleteCount = repository.removeOneFromCollectionById(CollectionNames.SESSION, id);
        assertEquals(deleteCount, 1);
        assertNull(repository.findOneFromCollection(CollectionNames.SESSION, test1));

        List<Document> docs = repository.findAllFromCollectionBySessionId(CollectionNames.SESSION, id);
        assertEquals(docs.size(), 0);
    }

    @Test
    public void removeOneFromCollectionByIdUUIDTest() {
        repository.insertIntoCollection(CollectionNames.SESSION, test1);

        UUID id = UUID.fromString(test1.getString("sessionId"));
        long deleteCount = repository.removeOneFromCollectionById(CollectionNames.SESSION, id);
        assertEquals(deleteCount, 1);
        assertNull(repository.findOneFromCollection(CollectionNames.SESSION, test1));

        List<Document> docs = repository.findAllFromCollectionBySessionId(CollectionNames.SESSION, id);
        assertEquals(docs.size(), 0);
    }

    @Test
    public void removeAllFromCollectionByIdStringTest() {
        String id = test1.getString("sessionId");
        long deleteCount = repository.removeAllFromCollectionById(CollectionNames.MONGO_TESTING, id);
        assertEquals(deleteCount, 2);
        assertNull(repository.findOneFromCollection(CollectionNames.MONGO_TESTING, test1));
        assertNull(repository.findOneFromCollection(CollectionNames.MONGO_TESTING, test2));
    }

    @Test
    public void removeAllFromCollectionByIdUUIDTest() {
        UUID id = UUID.fromString(test1.getString("sessionId"));
        long deleteCount = repository.removeAllFromCollectionById(CollectionNames.MONGO_TESTING, id);
        assertEquals(deleteCount, 2);
        assertNull(repository.findOneFromCollection(CollectionNames.MONGO_TESTING, test1));
        assertNull(repository.findOneFromCollection(CollectionNames.MONGO_TESTING, test2));
    }

    @Test
    public void updateOneFromCollectionByIdStringTest() {
        String id = test1.getString("sessionId");

        Document updated = new Document("name", "updated")
                .append("sessionId", "7dc53df5-703e-49b3-8670-b1c468f47f1f");

        boolean wasModified = repository.updateOneFromCollectionBySessionId(CollectionNames.MONGO_TESTING, id, updated);

        assertTrue(wasModified);
        assertEquals(repository.findOneFromCollectionBySessionId(CollectionNames.MONGO_TESTING, id).getString("name"), updated.getString("name"));
        assertEquals(repository.findOneFromCollectionBySessionId(CollectionNames.MONGO_TESTING, id).getString("sessionId"), updated.getString("sessionId"));
        assertEquals(repository.findAllFromCollectionBySessionId(CollectionNames.MONGO_TESTING, id).size(), 2);
    }

    @Test
    public void updateOneFromCollectionByIdUUIDTest() {
        UUID id = UUID.fromString(test1.getString("sessionId"));

        Document updated = new Document("name", "updated")
                .append("sessionId", "7dc53df5-703e-49b3-8670-b1c468f47f1f");

        boolean wasModified = repository.updateOneFromCollectionBySessionId(CollectionNames.MONGO_TESTING, id, updated);

        assertTrue(wasModified);
        assertEquals(repository.findOneFromCollectionBySessionId(CollectionNames.MONGO_TESTING, id).getString("name"), updated.getString("name"));
        assertEquals(repository.findOneFromCollectionBySessionId(CollectionNames.MONGO_TESTING, id).getString("sessionId"), updated.getString("sessionId"));
        assertEquals(repository.findAllFromCollectionBySessionId(CollectionNames.MONGO_TESTING, id).size(), 2);
    }

    @Test
    public void updateUserTest() {
        var userId = UUID.randomUUID();
        var user = User.builder().userId(userId).build().toDocument();
        var updatedUser = User.builder().userId(userId).nickname("new").build().toDocument();
        var searchParams = new Document("nickname", "new");

        repository.insertIntoCollection(CollectionNames.USER, user);
        assertTrue(repository.updateUser(updatedUser));

        assertEquals(repository.findOneFromCollection(CollectionNames.USER, searchParams).getString("nickname"), "new");
        assertEquals(1, repository.removeOneFromCollectionById(CollectionNames.USER, userId), 0);
    }

    @Test
    public void purgeAllBySessionTest() {
        var id = UUID.randomUUID().toString();
        var doc = new Document("name", "test").append("sessionId", id);

        repository.insertIntoCollection(CollectionNames.USER, doc);
        repository.insertIntoCollection(CollectionNames.SESSION, doc);
        repository.insertIntoCollection(CollectionNames.QUEUE, doc);
        repository.insertIntoCollection(CollectionNames.SETTING, doc);
        repository.insertIntoCollection(CollectionNames.MONGO_TESTING, doc);

        assertEquals(5, repository.purgeAllBySessionId(id), 0);
    }

    @After
    public void dropCollection() {
        repository.dropCollection(CollectionNames.MONGO_TESTING.toString());
    }
}