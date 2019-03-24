package tracker.timestamp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TimeStampMongoDB {

    private String assigneeName;
    private String spentTimeToday;
    private String spentTimeMonth;
    private String overTime;
    private String overTimeLate;
    private String overTimeWeekend;

    public String toJson() {

        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return null;
    }
}
