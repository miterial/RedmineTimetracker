package tracker;

import com.taskadapter.redmineapi.RedmineAuthenticationException;
import com.taskadapter.redmineapi.RedmineException;
import database.MongoDB;
import lombok.Getter;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tracker.timestamp.TimeStampMongoDB;
import tracker.timestamp.TimeStampRedmine;
import utils.Utils;

public class TrackerModel {

    private final String ZERO_TIME = "00:00";

    private Logger LOG = LoggerFactory.getLogger(TrackerModel.class);

    @Getter
    private MongoDB mongoDB;
    @Getter
    private RedmineInfo redmineInfo;

    private String username;

    /**
     * Connect to Redmine using login information
     *
     * @param username
     * @param password
     */
    public void connectWithLoginRedmine(String username, String password) throws RedmineException {

        LOG.info("Connecting to redmine...");

        redmineInfo = new RedmineInfo(username, password);

        this.username = username;

        LOG.info("Connected to Redmine successfully");
    }

    /**
     * Initialize MongoDB database
     */
    public void initDatabase() {

        LOG.info("Connect to database");

        this.mongoDB = new MongoDB(redmineInfo.getCurrentUserName());
    }

    /**
     * Save last time value spent on current issue
     *
     * @param currentIssueId int
     * @param currentProjectId int
     * @param timeDifference String time spent doing task
     * @param spentTimeToday String time spent today on all tasks
     * @param comment Sting
     */
    public void createTimestamp(int currentIssueId, int currentProjectId, String timeDifference,
                                String spentTimeToday, String comment) {

        createMongoTimeEntry(timeDifference, spentTimeToday);

        LOG.info("Creating Redmine timestamp for issueID=" + currentIssueId + "...");

        TimeStampRedmine timeStampRedmine = new TimeStampRedmine(username, currentIssueId, currentProjectId,
                Utils.convertToFloat(timeDifference), comment);

        LOG.info("Redmine timestamp created for issueID=" + currentIssueId);

        redmineInfo.createRedmineTimeEntry(timeStampRedmine);

    }

    public void createMongoTimeEntry(String timeDifference, String spentTimeToday) throws NullPointerException {

        String spentTimeMonth;

        if(mongoDB.getMongoClient() == null)
            mongoDB.connect();

        if(mongoDB.checkIfUserExists()) {
            spentTimeMonth = Utils.convertToString(String.valueOf(Utils.convertToFloat(timeDifference) +
                    Utils.convertToFloat(String.valueOf(mongoDB.getFieldValue("spentTimeMonth")))));
        }
        else {
            spentTimeMonth = spentTimeToday;
        }
        LOG.debug("spentTimeMonth: " + spentTimeMonth);

        // TODO: calculate overtime
        LocalTime localTime = new LocalTime();
        if(localTime.getHourOfDay() > 18) {}
        if(localTime.getHourOfDay() > 21) {}

        TimeStampMongoDB timeStampMongo = new TimeStampMongoDB(username,
                spentTimeToday, spentTimeMonth,
                ZERO_TIME, ZERO_TIME, ZERO_TIME);

        mongoDB.markTimeToDB(timeStampMongo.toJson());

        LOG.info("Timestamp in MongoDB created");
    }
}
