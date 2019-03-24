package mongoDB;

import com.fasterxml.jackson.databind.ObjectMapper;
import database.MongoDB;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.*;
import tracker.timestamp.TimeStampMongoDB;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class MongoBaseTest {

    static Logger log = LoggerFactory.getLogger(MongoTimestampTypesTest.class);

    protected static TimeStampMongoDB timeStamp;
    protected static MongoDB mongoDB;

    /**
     * Create instance of MongoDB class and get init parameters from file
     *
     * @param path path to settings file
     */
    @BeforeSuite
    @Parameters({"testdb_timestamp_path", "mdb_username"})
    public void init(String path, String name) throws UnsupportedEncodingException {

        mongoDB = new MongoDB(name);
        mongoDB.connect();
        ObjectMapper mapper = new ObjectMapper();
        //Object to JSON in file
        try {
            timeStamp = mapper.readValue(new File(path), TimeStampMongoDB.class);
            timeStamp.setAssigneeName(name);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterSuite
    public void end() {

        mongoDB.getDatabase().getCollection("tracker").deleteMany(new Document());
        mongoDB.close();
    }

}
