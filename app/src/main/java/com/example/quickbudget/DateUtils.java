package com.example.quickbudget;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * DateUtils
 * ----------
 * Classe utilitária para manipulação e formatação de datas no contexto da aplicação.
 * Fornece métodos para:
 *  - Calcular o início e fim da semana atual
 *  - Gerar intervalos semanais em texto
 *  - Criar rótulos de semanas anteriores
 *  - Formatar datas para exibição
 */
public class DateUtils {

    /**
     * Formata uma data (timestamp em milissegundos)
     * para o formato legível "dd MMM yyyy".
     *
     * @param millis timestamp a converter
     * @return string com a data formatada
     */
    public static String formatDate(long millis) {
        return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(millis);
    }

    /**
     * Retorna o intervalo textual da semana atual.
     * Exemplo: "04 - 10 Nov".
     */
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

    /**
     * Gera uma lista de rótulos representando as últimas N semanas.
     * Cada rótulo segue o formato "dd MMM - dd MMM".
     *
     * @param n número de semanas a incluir
     * @return lista de strings com os intervalos semanais
     */
    public static List<String> getLastWeeksLabels(int n) {
        List<String> labels = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());

        for (int i = n - 1; i >= 0; i--) {
            long[] range = getWeekRangeFromNowOffset(-i);
            labels.add(sdf.format(range[0]) + " - " + sdf.format(range[1]));
        }
        return labels;
    }

    /**
     * Retorna o timestamp correspondente ao início da semana atual
     * (segunda-feira às 00:00).
     */
    public static long getWeekStartMillis() {
        return getWeekStartCalendar().getTimeInMillis();
    }

    /**
     * Retorna o timestamp do fim da semana atual
     * (domingo às 23:59:59).
     */
    public static long getWeekEndMillis() {
        Calendar cal = getWeekStartCalendar();
        cal.add(Calendar.DAY_OF_MONTH, 6);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    /**
     * Cria um objeto Calendar ajustado para o início da semana atual.
     * Define segunda-feira como o primeiro dia da semana.
     */
    private static Calendar getWeekStartCalendar() {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    /**
     * Calcula o início e fim de uma semana com base num deslocamento
     * (positivo ou negativo) em relação à semana atual.
     *
     * @param offsetWeeks número de semanas a deslocar (ex: -1 = semana passada)
     * @return array com dois timestamps: [início, fim]
     */
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
}
