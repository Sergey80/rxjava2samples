package rxjava3.samples.steps;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogUtil {

    public static void logWithCurrentTime(final String text) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String formatDateTime = LocalDateTime.now().format(formatter);

        System.out.println(formatDateTime + ": " + text);

    }

}
