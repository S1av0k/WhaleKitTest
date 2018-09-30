package org.home.whalekit;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * Project WhaleKitTest
 * Created by s1av0k on 29.09.2018.
 */
public class DbService {

    // TODO Запихать настройки подключения в properties
    private static final String DB_NAME = "testdb";
    private static final String DB_HOST = "localhost";
    private static final int DB_PORT = 27017;

    private static final String COLLECTION_PLAYER = "player";
    private static final String FIELD_ID_SYSTEM = "_id";

    private MongoDatabase database;

    private static volatile DbService instance;


    private DbService() {
        MongoClient mongoClient = new MongoClient(DB_HOST, DB_PORT);
        database = mongoClient.getDatabase(DB_NAME);
    }

    public static DbService getInstance() {
        if (instance == null) {
            synchronized (DbService.class) {
                if (instance == null) {
                    instance = new DbService();
                }
            }
        }
        return instance;
    }

    public DTOPlayer findPlayerById(String playerId) {
        MongoCollection<Document> players = database.getCollection(COLLECTION_PLAYER);

        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put(FIELD_ID_SYSTEM, playerId);

        FindIterable<Document> a = players.find(searchQuery);
        MongoCursor<Document> iterator = a.iterator();

        if (iterator.hasNext()) {
            Document document = iterator.next();

            DTOPlayer player = new DTOPlayer();
            player.setId(document.getString(FIELD_ID_SYSTEM));
            player.setLikes(document.getLong("likes"));

            return player;
        }
        return null;
    }

    public DTOPlayer createPlayerIfNotExist(DTOPlayer dto) {
        DTOPlayer player = findPlayerById(dto.getId());
        if (player == null) {
            // Критическая секция нужна только в первый раз или не пригодится вовсе, если
            // игрок с указанным ID уже будет в БД
            synchronized (this) {
                Document insertDocument = new Document();
                insertDocument.put(FIELD_ID_SYSTEM, dto.getId());
                insertDocument.put("likes", 0L);

                MongoCollection<Document> players = database.getCollection(COLLECTION_PLAYER);
                players.insertOne(insertDocument);

                player = findPlayerById(dto.getId());
            }
        }
        return player;
    }

    /** Инкремент лайков. Может вызываться из нескольких потоков */
    public void incrementLike(DTOPlayer dto) {
        MongoCollection<Document> players = database.getCollection(COLLECTION_PLAYER);

        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put(FIELD_ID_SYSTEM, dto.getId());

        BasicDBObject newDocument =
                new BasicDBObject().append("$inc",
                        new BasicDBObject().append("likes", 1));

        players.findOneAndUpdate(searchQuery, newDocument);
    }

    /** Очистить коллекцию */
    public void dropPlayerCollection() {
        MongoCollection<Document> players = database.getCollection(COLLECTION_PLAYER);
        players.drop();
    }
}