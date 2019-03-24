package tracker.timestamp;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.bean.TimeEntry;
import com.taskadapter.redmineapi.bean.TimeEntryFactory;
import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
public class TimeStampRedmine {

    @NonNull
    private String userName;
    @NonNull
    private int issueID;
    @NonNull
    private int projectID;
    @NonNull
    private float timeSpent;
    @NonNull
    private String comment;

    /**
     * Create Redmine timestamp with needed information
     *
     * @return redmine's TimeEntry
     */
    public TimeEntry createRedmineTimestamp() {

        // TODO: time entry creates incorrectly
        final TimeEntry template = TimeEntryFactory.create();
        template.setUserName(this.userName);
        template.setHours(this.timeSpent);
        template.setActivityName("Developing");
        template.setActivityId(9); // id = 9 is for Development activity
        template.setIssueId(this.issueID);
        template.setProjectId(this.projectID);
        template.setComment(this.comment);

        return template;
    }
}
