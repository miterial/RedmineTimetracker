package database;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoServerException;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import lombok.*;
import org.bson.Document;
import org.bson.codecs.configuration.CodecConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;

@Getter
@Setter
public class MongoDB {

    static Logger MONGODB_LOG = LoggerFactory.getLogger(MongoDB.class);

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoDBConfig mongoDBConfig;
    private MongoCollection<Document> collection;

    private String currentUser;

    public MongoDB(String username) {
        loadSettings("src/main/resources/connect_data.json");
        this.currentUser = username;
    }

    /**
     * Load connection settings from file
     *
     * @param path path to settings file
     */
    public void loadSettings(String path)  {

        ObjectMapper mapper = new ObjectMapper();
        //Object to JSON in file
        try {
            mongoDBConfig = mapper.readValue(new File(path), MongoDBConfig.class);
            mongoDBConfig.setUri();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Connect to MongoDB database
     */
    public void connect() {

        MONGODB_LOG.info("Connection URI:" + mongoDBConfig.getUri());

        try {

            createMongoClient();
            connectToDB();

        } catch (NullPointerException e) {
            MONGODB_LOG.error("Could not connect to database!");
            // TODO: tell user about it
        }

        MONGODB_LOG.info("Connected to DB: " + mongoDBConfig.getDbName());
    }

    /**
     * Initialize database
     */
    private void connectToDB() throws NullPointerException {

        if(database == null){

            database = mongoClient.getDatabase(mongoDBConfig.getDbName());
            collection = database.getCollection(mongoDBConfig.getCollection());
        }
        else MONGODB_LOG.info("Database connection is already created");
    }

    /**
     * Create connection to MongoDB
     */
    private void createMongoClient() throws NullPointerException {

        if (mongoClient == null) {
            mongoClient = MongoClients.create(mongoDBConfig.getUri());
            if(mongoClient == null) {
                throw new NullPointerException();
            }
        }
        else MONGODB_LOG.info("MongoClient already exists");

    }

    /**
     * Close MongoDB connection (used in tests)
     */
    public void close() {

        MONGODB_LOG.info("Connection to DB " + mongoDBConfig.getDbName() + " closed");
        mongoClient.close();

    }

    /**
     * Update MongoDB collection with new time value
     *
     * @param timeStamp object with information about last timestamp
     */
    public boolean markTimeToDB(String timeStamp) throws CodecConfigurationException {

        if(mongoClient == null || database == null)
            connect();

        MONGODB_LOG.info("Using collection: " + collection);

        Document doc = Document.parse(timeStamp);
        try {
            if(checkIfUserExists())
                collection.replaceOne(eq("assigneeName", this.currentUser), doc);
            else collection.insertOne(doc);
        } catch (MongoServerException ex) {
            return false;
        }

        return true;
    }

    public boolean checkIfUserExists() {

        if(mongoClient == null || database == null)
            connect();

        return collection.find(eq("assigneeName", this.currentUser)).first() != null;
    }

    /**
     * Get field value for current user from db by key
     *
     * @param key key to find
     * @return value by key
     */
    public Object getFieldValue(String key) {

        if(mongoClient == null || database == null)
            connect();

        //get the only document for current user
        Document res = collection.find(eq("assigneeName", this.currentUser)).first();
        if(res != null)
            //return users's value by key
            return res.get(key);

        return null;
    }

    public String getUserInfo() {

        if(mongoClient == null || database == null)
            connect();

        //get the only document for current user
        Document res = collection.find(eq("assigneeName", this.currentUser)).first();

        if(res == null) {
            return null;
        }

        return res.toJson();
    }
}
