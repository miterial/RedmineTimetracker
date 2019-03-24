package redmine.search;

import com.taskadapter.redmineapi.*;
import com.taskadapter.redmineapi.bean.Issue;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import tracker.RedmineInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class getIssues {

    RedmineInfo redmineInfo;

    /**
     * Inititalize redmine login manager
     *
     * @param uri company address on redmine
     * @param name redmine username
     * @param pass redmine password
     */
    @Test
    @Parameters({"r_uri", "r_username", "r_password"})
    public void initManager(String uri, String name, String pass) throws RedmineException {

        redmineInfo = new RedmineInfo(name, pass);

        Assert.assertNotNull(redmineInfo.getMgr());
    }

    /**
     * Get issues using filter parameters of redmine
     *
     * @param userID filter issues assigned to userID
     * @param projectID filter issues for {projectID} project
     */
    @Test(dependsOnMethods = "initManager")
    @Parameters({"r_userID", "r_projectID"})
    public void getIssues_byQuery(int userID, int projectID) {

        Params params = new Params()
                .add("set_filter", "1")
                .add("f[]", "assigned_to_id")
                .add("op[assigned_to_id]", "~")
                .add("v[assigned_to_id][]", String.valueOf(userID))
                .add("f[]", "project_id")
                .add("op[project_id]", "~")
                .add("v[project_id][]", String.valueOf(projectID));

        List<Issue> issues = null;
        try {
            issues = redmineInfo.getMgr().getIssueManager().getIssues(params).getResults();
        } catch (RedmineException e) {
            e.printStackTrace();
        }

        Assert.assertNotNull(issues);

    }

    @Test(dependsOnMethods = "initManager")
    @Parameters({"r_projectID"})
    public void getIssues_fromProjects(int projectID) {

        Map<String, Integer> issuesMap = new HashMap<>();

        redmineInfo.setIssues();

        for(Issue i: redmineInfo.getIssues()) {
            if(i.getProjectId() == projectID) {
                issuesMap.put(i.getSubject(), i.getId());
            }
        }

        Assert.assertNotEquals(issuesMap.size(), 0);

    }
}
