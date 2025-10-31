package com.example.quickbudget;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Gere o armazenamento de despesas via SharedPreferences.
 * Suporta despesas recorrentes (Semanal / Mensal).
 */
public class DespesaStorage {
    private static final String PREFS_NAME = "despesas_prefs";
    private static final String KEY_DESPESAS = "todas_despesas";

    // Guarda uma nova despesa.
    public static void salvarDespesa(Context ctx, Despesa despesa) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        List<Despesa> lista = getDespesas(ctx);
        lista.add(despesa);
        saveAll(ctx, lista);
    }

    // Guarda todas as despesas no SharedPreferences.
    private static void saveAll(Context ctx, List<Despesa> lista) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = Despesa.toJson(lista);
        prefs.edit().putString(KEY_DESPESAS, json).apply();
    }

    // Obtém todas as despesas.
    public static List<Despesa> getDespesas(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_DESPESAS, "[]");
        return Despesa.fromJsonList(json);
    }

    // Obtém despesas da semana atual.
    public static List<Despesa> getDespesasSemana(Context ctx) {
        List<Despesa> todas = getDespesas(ctx);
        List<Despesa> semana = new ArrayList<>();
        long start = DateUtils.getWeekStartMillis();
        long end = DateUtils.getWeekEndMillis();
        for (Despesa d : todas) {
            if (d.getTimestamp() >= start && d.getTimestamp() <= end)
                semana.add(d);
        }
        return semana;
    }

    // Obtém n últimas despesas (por data).
    public static List<Despesa> getDespesasRecentes(Context ctx, int n) {
        List<Despesa> todas = getDespesas(ctx);
        todas.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        return todas.subList(0, Math.min(n, todas.size()));
    }

    // Total gasto na semana atual.
    public static double getTotalGastoSemana(Context ctx) {
        double total = 0.0;
        for (Despesa d : getDespesasSemana(ctx)) total += d.getValor();
        return total;
    }

    // Total gasto numa semana específica.
    public static double getTotalGastoSemana(Context ctx, Calendar semana) {
        long start = DateUtils.getWeekStartMillis(semana);
        long end = DateUtils.getWeekEndMillis(semana);
        double total = 0.0;
        for (Despesa d : getDespesas(ctx)) {
            if (d.getTimestamp() >= start && d.getTimestamp() <= end)
                total += d.getValor();
        }
        return total;
    }

    // Total gastos para as n últimas semanas.
    public static List<Double> getTotalGastosSemanas(Context ctx, int nSemanas) {
        List<Double> totais = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        for (int i = nSemanas - 1; i >= 0; i--) {
            Calendar c = (Calendar) cal.clone();
            c.add(Calendar.WEEK_OF_YEAR, -i);
            totais.add(getTotalGastoSemana(ctx, c));
        }
        return totais;
    }

    // Atualiza despesa pelo id.
    public static void atualizarDespesa(Context ctx, Despesa nova) {
        List<Despesa> lista = getDespesas(ctx);
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getId().equals(nova.getId())) {
                lista.set(i, nova);
                break;
            }
        }
        saveAll(ctx, lista);
    }

    // Elimina despesa pelo id.
    public static void eliminarDespesa(Context ctx, String id) {
        List<Despesa> lista = getDespesas(ctx);
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getId().equals(id)) {
                lista.remove(i);
                break;
            }
        }
        saveAll(ctx, lista);
    }

    // Limpa todas as despesas.
    public static void limparTodasDespesas(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_DESPESAS).apply();
    }

    // Verifica recorrentes e adiciona caso ainda não estejam lançadas para semana/mês
    public static void verificarDespesasRecorrentes(Context ctx) {
        List<Despesa> todas = getDespesas(ctx);
        List<Despesa> novas = new ArrayList<>();
        long semanaAtual = DateUtils.getWeekStartMillis();
        Calendar cal = Calendar.getInstance();
        for (Despesa d : todas) {
            if ("Semanal".equalsIgnoreCase(d.getRecorrencia())) {
                cal.setTimeInMillis(semanaAtual);
                boolean existe = false;
                for (Despesa existente : todas) {
                    if (existente.getDescricao().equals(d.getDescricao()) &&
                            existente.getCategoria().equals(d.getCategoria()) &&
                            existente.getValor() == d.getValor() &&
                            existente.getRecorrencia().equals(d.getRecorrencia()) &&
                            DateUtils.getWeekStartMillis(existente.getTimestamp()) == semanaAtual) {
                        existe = true;
                        break;
                    }
                }
                if (!existe) {
                    Despesa nova = new Despesa(d.getDescricao(), d.getCategoria(), d.getValor(), System.currentTimeMillis(), d.getRecorrencia());
                    novas.add(nova);
                }
            }
            if ("Mensal".equalsIgnoreCase(d.getRecorrencia())) {
                cal.setTimeInMillis(System.currentTimeMillis());
                int mesAtual = cal.get(Calendar.MONTH);
                boolean existe = false;
                for (Despesa existente : todas) {
                    cal.setTimeInMillis(existente.getTimestamp());
                    if (existente.getDescricao().equals(d.getDescricao()) &&
                            existente.getCategoria().equals(d.getCategoria()) &&
                            existente.getValor() == d.getValor() &&
                            existente.getRecorrencia().equals(d.getRecorrencia()) &&
                            cal.get(Calendar.MONTH) == mesAtual) {
                        existe = true;
                        break;
                    }
                }
                if (!existe) {
                    Despesa nova = new Despesa(d.getDescricao(), d.getCategoria(), d.getValor(), System.currentTimeMillis(), d.getRecorrencia());
                    novas.add(nova);
                }
            }
        }
        if (!novas.isEmpty()) {
            todas.addAll(novas);
            saveAll(ctx, todas);
        }
    }
}
