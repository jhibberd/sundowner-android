package com.example.geotag;

import android.text.format.DateFormat;

import java.util.Calendar;

public class DetailFormatter {

    private static final int SECONDS_PER_HOUR = 3600;
    private static final int SECONDS_PER_DAY = SECONDS_PER_HOUR * 24;
    private static final int SECONDS_PER_MONTH = SECONDS_PER_DAY * 30; // average days in month

    private static boolean initMaxWeekday;
    private static int maxWeekday;

    public String formatDetail(double distance, long timestamp, String username) {
        String formattedDistance = formatDistance(distance);
        String formattedTimestamp = formatTimestamp(timestamp);
        return String.format("%s, %s by %s", formattedDistance, formattedTimestamp, username);
    }

    private String formatTimestamp(long timestamp) {

        /* Testing resource:
         * http://www.4webhelp.net/us/timestamp.php
         */
        long now = System.currentTimeMillis() / 1000L;
        long delta = now - timestamp;

        /* Date format specifiers:
         * http://www.unicode.org/reports/tr35/tr35-25.html#Date_Format_Patterns
         *
         * Strings that aren't supported by the date formatter will have to be translated by
         * another mechanism into the user's locale (eg. "Yesterday").
         */
        String formatString;
        if (delta > SECONDS_PER_MONTH * 11) {
            /* 11 months is used instead of 12 because if something happened 12 months ago it will
             * have happened during the same month as now, which would be confusing.
             */
            formatString = "d MMMM yyyy"; // 3 August 2011

        } else if (delta > SECONDS_PER_MONTH) {
            formatString = "d MMMM"; // 3 August

        } else if (delta > SECONDS_PER_DAY * 6) {
            /* 6 days is used instead of 7 because if something happened 7 days ago it will have
             * happened on the same day of the week as now, which would be confusing.
             */
            formatString = "d MMMM 'at' k':'mm"; // 3 August at 14:22

        } else if (delta >= SECONDS_PER_HOUR * 2) {

            // get the weekday for the timestamp and now
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp * 1000L);
            int timestampWeekday = calendar.get(Calendar.DAY_OF_WEEK);
            calendar.setTimeInMillis(now * 1000L);
            int nowWeekday = calendar.get(Calendar.DAY_OF_WEEK);

            if (timestampWeekday == nowWeekday) {
                long hours = delta / SECONDS_PER_HOUR;
                formatString = String.format("'%d hours ago'", hours); // 4 hours ago

            } else if (isYesterday(timestampWeekday, nowWeekday)) {
                formatString = "'yesterday at' k':'mm"; // Yesterday at 14:22

            } else {
                formatString = "EEEE 'at' k':'mm"; // Thursday at 14:22
            }

        } else if (delta >= SECONDS_PER_HOUR) {
            formatString = "'1 hour ago'"; // 1 hour ago

        } else {
            formatString = "moments ago";
        }

        CharSequence result = DateFormat.format(formatString, timestamp * 1000L);
        return result.toString();
    }

    private boolean isYesterday(int candidateDay, int anchorDay) {

        if (!initMaxWeekday) {
            Calendar calendar = Calendar.getInstance();
            maxWeekday = calendar.getActualMaximum(Calendar.DAY_OF_WEEK);
            initMaxWeekday = true;
        }

        if (anchorDay > 1) {
            return candidateDay == anchorDay - 1;
        } else {
            return candidateDay == maxWeekday;
        }
    }

    private String formatDistance(double distance) {

        // distance is measured in meters
        if (distance > 100000) {
            return "far, far away";
        } else if (distance > 10000) {
            return String.format("%.0f km", distance / 1000);
        } else if (distance > 1000) {
            return String.format("%.2f km", distance / 1000);
        } else if (distance > 10) {
            return String.format("%.0f meters", distance);
        } else if (distance > 5) {
            return String.format("%.2f meters", distance);
        } else {
            return "here";
        }
    }
}
