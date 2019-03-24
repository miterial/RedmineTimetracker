package mongoDB;

import org.testng.Assert;
import org.testng.annotations.*;

import static com.mongodb.client.model.Filters.eq;

public class MongoTimestampTypesTest extends MongoBaseTest {

    /**
     * Dataprovider with different basic types of data
     *
     * @return array of Objects
     */
    @DataProvider
    private Object[] data() {

        return new Object[] {
                1,
                1.1,
                "01:01",
                "properString",
                "kdtng!@#$%^&*()_+454/"
        };
    }

    @Test(dataProvider = "data")
    public void SendOvertime(Object time) {

        timeStamp.setOverTime(String.valueOf(time));
        saveTimestamp();
        log.info("overTime Test");
        Assert.assertNotNull(mongoDB.getFieldValue("overTime"));
    }

    @Test(dataProvider = "data")
    public void SendOvertimeLate(Object time) {

        timeStamp.setOverTimeLate(String.valueOf(time));
        saveTimestamp();
        log.info("overTimeLate Test");
        Assert.assertNotNull(mongoDB.getFieldValue("overTimeLate"));
    }

    @Test(dataProvider = "data")
    public void SendOvertimeWeekend(Object time) {

        timeStamp.setOverTimeWeekend(String.valueOf(time));
        saveTimestamp();
        log.info("overTimeWeekend Test");
        Assert.assertNotNull(mongoDB.getFieldValue("overTimeWeekend"));
    }

    /**
     * Create new document in Mongodb with updated parameters
     */
    private void saveTimestamp() {

        mongoDB.markTimeToDB(timeStamp.toJson());
    }

}
