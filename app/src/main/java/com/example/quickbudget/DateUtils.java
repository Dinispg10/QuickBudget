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

    // 🗓️ Retorna o intervalo da semana atual em formato "03 - 09 Nov"
    public static String getCurrentWeekRangeString() {
        Calendar start = getWeekStartCalendar();
        Calendar end = (Calendar) start.clone();
        end.add(Calendar.DAY_OF_MONTH, 6);
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
        return sdf.format(start.getTime()) + " - " + sdf.format(end.getTime());
    }

    // 📊 Retorna os rótulos (labels) das últimas N semanas, ex: "28 Out - 03 Nov"
    public static List<String> getLastWeeksLabels(int n) {
        List<String> labels = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());

        for (int i = n - 1; i >= 0; i--) {
            long[] range = getWeekRangeFromNowOffset(-i);
            labels.add(sdf.format(range[0]) + " - " + sdf.format(range[1]));
        }

        return labels;
    }

    // 📅 Início da semana atual (segunda-feira 00:00)
    public static long getWeekStartMillis() {
        return getWeekStartCalendar().getTimeInMillis();
    }

    // 📅 Fim da semana atual (domingo 23:59)
    public static long getWeekEndMillis() {
        Calendar cal = getWeekStartCalendar();
        cal.add(Calendar.DAY_OF_MONTH, 6);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    // ✅ Retorna o calendário ajustado para o início da semana (segunda-feira)
    // ✅ Corrigido: domingo pertence à semana atual (não à próxima)
    private static Calendar getWeekStartCalendar() {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setFirstDayOfWeek(Calendar.MONDAY);

        // 👉 NÃO avançar a semana se for domingo
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }


    // 🔧 Função auxiliar: devolve início e fim da semana de acordo com o offset
    public static long[] getWeekRangeFromNowOffset(int offsetWeeks) {
        Calendar start = getWeekStartCalendar();
        start.add(Calendar.WEEK_OF_YEAR, offsetWeeks);

        Calendar end = (Calendar) start.clone();
        end.add(Calendar.DAY_OF_MONTH, 6);
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);

        return new long[]{start.getTimeInMillis(), end.getTimeInMillis()};
    }

    // 🔍 Verifica se um timestamp pertence à semana atual
    public static boolean isInCurrentWeek(long timestamp) {
        return timestamp >= getWeekStartMillis() && timestamp <= getWeekEndMillis();
    }
}
