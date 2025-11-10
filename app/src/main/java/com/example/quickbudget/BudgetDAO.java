package com.example.quickbudget;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * BudgetDAO
 * ----------
 * Classe responsável por gerir as operações relacionadas com o orçamento semanal.
 * Permite guardar, ler e recuperar o valor do orçamento da base de dados local (SQLite),
 * assegurando que cada semana tem o seu próprio registo.
 */
public class BudgetDAO {
    private final SQLiteDatabase db;

    // Abre ligação à base de dados
    public BudgetDAO(Context ctx) {
        DBHelper helper = new DBHelper(ctx);
        db = helper.getWritableDatabase();
    }

    // Guarda ou substitui o orçamento da semana atual
    public void setBudget(double valor, long ignoredStartOfWeek) {
        long startOfWeek = DateUtils.getWeekStartMillis(); // início real da semana

        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_WEEK_START, startOfWeek);
        values.put(DBHelper.COLUMN_BUDGET_VALUE, valor);

        // Substitui caso já exista registo para essa semana
        db.insertWithOnConflict(DBHelper.TABLE_BUDGET, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    // Obtém o orçamento da semana atual ou cria se ainda não existir
    public double getOrCreateBudgetAtual(long startOfWeek) {
        Double existente = getBudgetValor(startOfWeek);
        if (existente != null) return existente;

        // Se não existir, tenta recuperar o da semana anterior
        Double anterior = getUltimoBudgetAntes(startOfWeek);
        if (anterior != null) {
            setBudget(anterior, startOfWeek);
            return anterior;
        }

        // Caso contrário, define 0€ como valor inicial
        setBudget(0.0, startOfWeek);
        return 0.0;
    }

    // Lê o orçamento definido para uma semana específica (sem criar novo)
    public double getBudgetPorSemana(long startOfWeek) {
        Double existente = getBudgetValor(startOfWeek);
        return existente != null ? existente : 0.0;
    }

    // Busca o valor do orçamento armazenado para a semana indicada
    private Double getBudgetValor(long startOfWeek) {
        Cursor c = db.query(
                DBHelper.TABLE_BUDGET,
                new String[]{DBHelper.COLUMN_BUDGET_VALUE},
                DBHelper.COLUMN_WEEK_START + "=?",
                new String[]{String.valueOf(startOfWeek)},
                null, null, null
        );

        Double valor = null;
        if (c.moveToFirst()) {
            valor = c.getDouble(c.getColumnIndexOrThrow(DBHelper.COLUMN_BUDGET_VALUE));
        }
        c.close();
        return valor;
    }

    // Busca o orçamento mais recente registado antes da semana atual
    private Double getUltimoBudgetAntes(long startOfWeek) {
        Cursor c = db.query(
                DBHelper.TABLE_BUDGET,
                new String[]{DBHelper.COLUMN_BUDGET_VALUE},
                DBHelper.COLUMN_WEEK_START + " < ?",
                new String[]{String.valueOf(startOfWeek)},
                null, null,
                DBHelper.COLUMN_WEEK_START + " DESC",
                "1"
        );

        Double valor = null;
        if (c.moveToFirst()) {
            valor = c.getDouble(c.getColumnIndexOrThrow(DBHelper.COLUMN_BUDGET_VALUE));
        }
        c.close();
        return valor;
    }

    // Fecha ligação à base de dados
    public void fechar() {
        db.close();
    }
}
