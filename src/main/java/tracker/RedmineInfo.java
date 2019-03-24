package tracker;

import com.taskadapter.redmineapi.*;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.TimeEntry;
import database.UserInfo;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tracker.timestamp.TimeStampRedmine;
import utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedmineInfo {

    Logger LOG = LoggerFactory.getLogger(TrackerModel.class);

    private String uri = "domain"; //TODO: get domain from external

    @Getter
    private RedmineManager mgr;
    private IssueManager issueManager = null;
    private UserManager userManager = null;
    private Params assignedIssuesParams;

    private int r_userID;
    @Getter
    private List<Issue> issues;



    public RedmineInfo(String name, String pass) throws RedmineException {

        mgr = RedmineManagerFactory.createWithUserAuth(uri, name, pass);

        mgr.getUserManager().getCurrentUser();

        r_userID = getCurrentUserId();

        assignedIssuesParams = new Params()
                .add("set_filter", "1")
                .add("f[]", "assigned_to_id")
                .add("op[assigned_to_id]", "~")
                .add("v[assigned_to_id][]", String.valueOf(r_userID));

        issues = new ArrayList<>();
    }

    /**
     * Get all projects from uri
     *
     * @return map of projects, string - project name, integer - project id
     */
    public Map<String, Integer> getProjects() {

        LOG.info("Getting projects' map");

        Map<String, Integer> projectsMap = new HashMap<>();

        try {

            setIssues();

            if(!issues.isEmpty())
            for(Issue issue : issues) {
                if(!projectsMap.containsKey(issue.getProjectName()))
                    projectsMap.put(issue.getProjectName(), issue.getProjectId());
            }

        }
        catch (NullPointerException e) {
            LOG.error("No projectsMap assigned");
        }

        return projectsMap;
    }

    public void setIssues() {

        try {

            if(issueManager == null) {

                issueManager = mgr.getIssueManager();
                LOG.info("Issue manager created...");
            }

            issues = issueManager.getIssues(assignedIssuesParams).getResults();

        } catch (RedmineException e) {
            e.printStackTrace();
        }
    }


    /**
     * Get issues by project ID
     *
     * @param projId projectsMap ID
     * @return map of issuesMap, string - issue subject, integer - issue id
     */
    public Map<String, Integer> getIssues(int projId) {

        LOG.info("Get issues from project, project ID=" + projId);

        Map<String, Integer> issuesMap = new HashMap<>();

        try {

            for(Issue issue : issues) {

                if(issue.getProjectId() == projId)
                    issuesMap.put(issue.getSubject(), issue.getId());
            }

        } catch (NullPointerException e) {
            LOG.error("No issues for this user found!");
        }

        return issuesMap;

    }

    /**
     * Get overall time spent on current issue by it's id
     *
     * @param currentIssueId
     * @return spent time (hh.mm)
     */
    public float getSpentTimeIssue(int currentIssueId) {

        LOG.info("Get spent time from issue id=" + currentIssueId);

        try {
            if(issueManager == null) {

                issueManager = mgr.getIssueManager();
                LOG.info("Issue manager created...");
            }

            return issueManager.getIssueById(currentIssueId).getSpentHours();

        } catch (RedmineException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Get current redmine user name
     *
     * @return string login
     */
    public String getCurrentUserName() {

        try {
            if(userManager == null) {

                userManager = mgr.getUserManager();
                LOG.info("Issue manager created...");
            }

            return userManager.getCurrentUser().getLogin();

        } catch (RedmineException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get current redmine user id
     *
     * @return int user id
     */
    public int getCurrentUserId() {
        int id = -1;
        try {
            if(userManager == null) {

                userManager = mgr.getUserManager();
                LOG.info("User manager created...");
            }
            id = userManager.getCurrentUser().getId();
        } catch (RedmineException e) {
            LOG.error("User ID not found");
        }
        LOG.info("Got user ID: " + id);
        return id;
    }

    /**
     * Create time entry in Redmine
     *
     * @param timeStampRedmine
     */
    public void createRedmineTimeEntry(TimeStampRedmine timeStampRedmine) {

        TimeEntry template = timeStampRedmine.createRedmineTimestamp();

        try {

            mgr.getTimeEntryManager().createTimeEntry(template);
            LOG.info("Timestamp in Redmine created");

        } catch (RedmineException e) {
            LOG.info("Timestamp in Redmine not created!");
            e.printStackTrace();
        }
    }

    public UserInfo getUserInfo() {

        UserInfo userInfo = new UserInfo(this);

        return userInfo;
    }
}
