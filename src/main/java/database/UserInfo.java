package database;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.bean.TimeEntry;
import tracker.RedmineInfo;
import utils.Utils;

import java.util.*;

/**
 * UserInfo is used when user starts using this app; it gathers information from Redmine
 */

public class UserInfo {

    private final String ZERO_TIME = "00:00";
    private String spentMonth;
    private String spentToday;

    List<TimeEntry> userTimeEntries;

    public UserInfo(RedmineInfo redmineInfo) {

        spentToday = ZERO_TIME;
        spentMonth = ZERO_TIME;

        redmineInfo.setIssues();

        int limit = 1000;

        Map<String, String> params = new HashMap<>();
        params.put("spent_on", "m");
        params.put("user_id", "me");
        params.put("limit", String.valueOf(limit));

        try {
            //TODO: only returns 1 page
            userTimeEntries = redmineInfo.getMgr().getTimeEntryManager().getTimeEntries(params).getResults();
        } catch (RedmineException e) {
            e.printStackTrace();
        }

        float month = 0, day = 0;
        int calendarDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        Calendar calTimeEntry = Calendar.getInstance();

        for(TimeEntry te: userTimeEntries) {

            month += te.getHours();

            calTimeEntry.setTime(te.getCreatedOn());
            int dayom = calTimeEntry.get(Calendar.DAY_OF_MONTH);

            if(dayom == calendarDay)
                day += te.getHours();
        }

        if(day > 0)
            spentToday = Utils.convertToString(String.valueOf(day));
        if(month > 0)
            spentMonth = Utils.convertToString(String.valueOf(month));
    }

    public String getSpentMonth() {
        return spentMonth;
    }

    public String getSpentToday() {
        return spentToday;
    }
}
