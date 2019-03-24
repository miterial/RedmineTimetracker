package utils;

import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

@Slf4j
public final class Utils {

    public static String convertToTime(String stringTime) {

        DateFormat formatter = new SimpleDateFormat( "HH:mm");
        String date = null;
        try {
            date = formatter.format(formatter.parse(stringTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }

    /**
     *
     *
     * @param inputString time
     * @return
     */
    public static float convertToFloat(String inputString)
    {
        inputString = inputString.replace(':','.');

        BigDecimal inputBD;
        try {
            inputBD = new BigDecimal(inputString);
        } catch (NumberFormatException e) {
            log.error("Incorrect time given " + '\n' + Arrays.toString(e.getStackTrace()));
            return 0;
        }
        String hhStr = inputString.split("\\.")[0];
        BigDecimal output = new BigDecimal(Float.toString(Integer.parseInt(hhStr)));
        output = output.add((inputBD.subtract(output).divide(BigDecimal.valueOf(60), 10, BigDecimal.ROUND_HALF_EVEN)).multiply(BigDecimal.valueOf(100)));

        log.info("Converted to float from string: " + output);
        return Float.parseFloat(output.toString());
    }

    // output format
    public static String convertToString(String inputString)
    {
        inputString = inputString.replace(':','.');
        BigDecimal inputBD = new BigDecimal(inputString.replace(':','.'));
        String hhStr = inputString.split("\\.")[0];
        BigDecimal output = new BigDecimal(Float.toString(Integer.parseInt(hhStr)));
        output = output.add((inputBD.subtract(output).multiply(BigDecimal.valueOf(60)).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_EVEN)));

        log.info("Converted to string from float: " + output);
        return output.toString().replace(".",":");
    }

    public static String getTimeDifference(String startTime, String endTime) {

        int startHours = Integer.parseInt(startTime.split("[:]")[0]);
        int startMinutes = Integer.parseInt(startTime.split("[:]")[1]);
        int endHours = Integer.parseInt(endTime.split("[:]")[0]);
        int endMinutes = Integer.parseInt(endTime.split("[:]")[1]);

        PeriodFormatter timeFormatter = new PeriodFormatterBuilder().printZeroAlways().minimumPrintedDigits(2).
                appendHours().appendSeparator(":").
                appendMinutes().toFormatter();

        DateTime start = new DateTime(1, 1, 1, startHours, startMinutes);
        DateTime end = new DateTime(1, 1, 1, endHours, endMinutes);

        Period period = new Period(start, end);

        String s = period.toString(timeFormatter);

        log.info("Timedifference: " + s);
        return s;
    }
}
