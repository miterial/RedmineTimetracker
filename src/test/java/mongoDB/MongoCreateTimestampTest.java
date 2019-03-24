package mongoDB;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MongoCreateTimestampTest extends MongoBaseTest {

    private String time = "01:10";

    @Test
    public void setTodayTime() {

        log.info("spentTimeToday : " + time);
        timeStamp.setSpentTimeToday(time);
        markTimestampValue("spentTimeToday");
    }

    @Test
    public void setMonthTime() {

        log.info("spentTimeMonth : " + time);
        timeStamp.setSpentTimeMonth(time);
        markTimestampValue("spentTimeMonth");
    }

    @Test
    public void setOverTime() {

        log.info("overTime : " + time);
        timeStamp.setOverTime(time);
        markTimestampValue("overTime");
    }

    @Test
    public void setOverTimeLate() {

        log.info("overTimeLate : " + time);
        timeStamp.setOverTimeLate(time);
        markTimestampValue("overTimeLate");
    }

    @Test
    public void setOverTimeWeekend() {

        log.info("overTimeWeekend : " + time);
        timeStamp.setOverTimeWeekend(time);
        markTimestampValue("overTimeWeekend");
    }

    private void markTimestampValue(String field) {

        mongoDB.markTimeToDB(timeStamp.toJson());

        Assert.assertEquals(mongoDB.getFieldValue(field), time);
    }
}
