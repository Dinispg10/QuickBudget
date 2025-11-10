package com.example.quickbudget;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class BudgetDAO {
    private final SQLiteDatabase db;

    // Abre ligação à base de dados
    public BudgetDAO(Context ctx) {
        DBHelper helper = new DBHelper(ctx);
        db = helper.getWritableDatabase();
    }

    // Guarda ou substitui o orçamento da semana
    public void setBudget(double valor, long ignoredStartOfWeek) {
        long startOfWeek = DateUtils.getWeekStartMillis(); // início da semana

        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_WEEK_START, startOfWeek);
        values.put(DBHelper.COLUMN_BUDGET_VALUE, valor);

        db.insertWithOnConflict(DBHelper.TABLE_BUDGET, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    // Obtém o orçamento da semana atual ou cria se não existir
    public double getOrCreateBudgetAtual(long startOfWeek) {
        Double existente = getBudgetValor(startOfWeek);
        if (existente != null) return existente;

        Double anterior = getUltimoBudgetAntes(startOfWeek);
        if (anterior != null) {
            setBudget(anterior, startOfWeek);
            return anterior;
        }

        setBudget(0.0, startOfWeek);
        return 0.0;
    }

    // Lê orçamento de uma semana (sem criar)
    public double getBudgetPorSemana(long startOfWeek) {
        Double existente = getBudgetValor(startOfWeek);
        return existente != null ? existente : 0.0;
    }

    // Busca valor do orçamento guardado para uma semana
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

    // Busca o orçamento mais recente antes da semana atual
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
