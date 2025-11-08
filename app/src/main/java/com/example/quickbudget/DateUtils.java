package com.example.quickbudget;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DateUtils {

    // 🗓️ Formata uma data em "dd MMM yyyy"
    public static String formatDate(long millis) {
        return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(millis);
    }

    // 🗓️ Retorna o intervalo da semana atual em formato "03 - 10 Nov"
    public static String getCurrentWeekRangeString() {
        Calendar now = Calendar.getInstance();
        Calendar start = (Calendar) now.clone();
        start.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Calendar end = (Calendar) start.clone();
        end.add(Calendar.DAY_OF_MONTH, 6);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
        return sdf.format(start.getTime()) + " - " + sdf.format(end.getTime());
    }

    // 📊 Retorna os rótulos (labels) das últimas N semanas, ex: "28 Out - 03 Nov"
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

            String label = sdf.format(start.getTime()) + " - " + sdf.format(end.getTime());
            labels.add(label);
        }
        return labels;
    }

    // 📅 Início da semana atual (segunda-feira 00h00)
    public static long getWeekStartMillis() {
        return getWeekStartMillis(Calendar.getInstance());
    }

    public static long getWeekStartMillis(Calendar c) {
        Calendar cal = (Calendar) c.clone();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return cal.getTimeInMillis();
    }

    // 📅 Fim da semana atual (domingo 23h59)
    public static long getWeekEndMillis() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(getWeekStartMillis());
        cal.add(Calendar.DAY_OF_MONTH, 6);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    // 🔢 Gera chave única mês+ano (ex: 202511)
    public static int getMonthYearKey(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        return cal.get(Calendar.YEAR) * 100 + cal.get(Calendar.MONTH);
    }
}
