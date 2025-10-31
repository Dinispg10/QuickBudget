package com.example.quickbudget;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Classe responsável por guardar e recuperar o orçamento semanal.
 * Suporta histórico real de budgets por semana e grava automaticamente
 * o orçamento da semana corrente quando o app abre.
 */
public class BudgetStorage {
    private static final String PREFS_NAME = "budget_prefs";
    private static final String KEY_WEEKLY_BUDGET = "weekly_budget";
    private static final String KEY_WEEKLY_BUDGET_PREFIX = "budget_week_";

    // Guarda o orçamento semanal atual e também associa à semana do ano (histórico)
    public static void setWeeklyBudget(Context ctx, double budget) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        // Atual
        editor.putLong(KEY_WEEKLY_BUDGET, Double.doubleToRawLongBits(budget));
        // Com semana-ano para histórico
        Calendar cal = Calendar.getInstance();
        int week = cal.get(Calendar.WEEK_OF_YEAR);
        int year = cal.get(Calendar.YEAR);
        editor.putLong(KEY_WEEKLY_BUDGET_PREFIX + week + "_" + year, Double.doubleToRawLongBits(budget));
        editor.apply();
    }

    public static double getWeeklyBudget(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long raw = prefs.getLong(KEY_WEEKLY_BUDGET, Double.doubleToRawLongBits(0.0));
        return Double.longBitsToDouble(raw);
    }

    public static double getBudgetForWeek(Context ctx, int week, int year) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long raw = prefs.getLong(KEY_WEEKLY_BUDGET_PREFIX + week + "_" + year, Double.doubleToRawLongBits(0.0));
        return Double.longBitsToDouble(raw);
    }

    public static List<Double> getBudgetsUltimasSemanas(Context ctx, int n) {
        List<Double> budgets = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        for (int i = n - 1; i >= 0; i--) {
            Calendar c = (Calendar) cal.clone();
            c.add(Calendar.WEEK_OF_YEAR, -i);
            int week = c.get(Calendar.WEEK_OF_YEAR);
            int year = c.get(Calendar.YEAR);
            budgets.add(getBudgetForWeek(ctx, week, year));
        }
        return budgets;
    }

    public static void ensureCurrentWeekBudgetStored(Context ctx) {
        Calendar cal = Calendar.getInstance();
        int week = cal.get(Calendar.WEEK_OF_YEAR);
        int year = cal.get(Calendar.YEAR);
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String key = KEY_WEEKLY_BUDGET_PREFIX + week + "_" + year;
        if (!prefs.contains(key)) {
            setWeeklyBudget(ctx, getWeeklyBudget(ctx));
        }
    }

    public static void clearAllBudgets(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
