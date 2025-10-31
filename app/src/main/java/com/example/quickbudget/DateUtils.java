package com.example.quickbudget;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DateUtils {
    public static String formatDate(long millis) {
        return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(millis);
    }

    public static String getCurrentWeekRangeString() {
        long start = getWeekStartMillis();
        long end = getWeekEndMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
        return sdf.format(start) + " - " + sdf.format(end);
    }

    public static List<String> getLastWeeksLabels(int n) {
        List<String> labels = new ArrayList<>();
        Calendar now = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
        for (int i = n - 1; i >= 0; i--) {
            Calendar start = (Calendar) now.clone();
            start.add(Calendar.WEEK_OF_YEAR, -i);
            start.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            Calendar end = (Calendar) start.clone();
            end.add(Calendar.DAY_OF_MONTH, 6);
            labels.add(sdf.format(start.getTime()) + " - " + sdf.format(end.getTime()));
        }
        return labels;
    }

    public static long getWeekStartMillis() {
        return getWeekStartMillis(Calendar.getInstance());
    }

    public static long getWeekStartMillis(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        return getWeekStartMillis(cal);
    }

    public static long getWeekStartMillis(Calendar c) {
        Calendar cal = (Calendar) c.clone();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) cal.add(Calendar.DAY_OF_MONTH, -1);
        return cal.getTimeInMillis();
    }

    public static long getWeekEndMillis() {
        return getWeekEndMillis(Calendar.getInstance());
    }

    public static long getWeekEndMillis(Calendar c) {
        Calendar cal = (Calendar) c.clone();
        cal.setTimeInMillis(getWeekStartMillis(cal));
        cal.add(Calendar.DAY_OF_MONTH, 6);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }


}
