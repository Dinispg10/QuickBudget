package com.example.quickbudget;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class BudgetDAO {
    private final SQLiteDatabase db;

    public BudgetDAO(Context ctx) {
        DBHelper helper = new DBHelper(ctx);
        db = helper.getWritableDatabase();
    }

    public void setBudget(double valor, long startOfWeek) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_WEEK_START, startOfWeek);
        values.put(DBHelper.COLUMN_BUDGET_VALUE, valor);
        db.insertWithOnConflict(DBHelper.TABLE_BUDGET, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public double getOrCreateBudgetAtual(long startOfWeek) {
        Cursor c = db.query(DBHelper.TABLE_BUDGET, null,
                DBHelper.COLUMN_WEEK_START + "=?", new String[]{String.valueOf(startOfWeek)},
                null, null, null);
        if (c.moveToFirst()) {
            double v = c.getDouble(c.getColumnIndexOrThrow(DBHelper.COLUMN_BUDGET_VALUE));
            c.close();
            return v;
        }
        c.close();

        // Se não existir, cria com 0
        setBudget(0.0, startOfWeek);
        return 0.0;
    }

    public double getBudgetPorSemana(long startOfWeek) {
        Cursor c = db.query(DBHelper.TABLE_BUDGET, null,
                DBHelper.COLUMN_WEEK_START + "=?", new String[]{String.valueOf(startOfWeek)},
                null, null, null);
        if (c.moveToFirst()) {
            double v = c.getDouble(c.getColumnIndexOrThrow(DBHelper.COLUMN_BUDGET_VALUE));
            c.close();
            return v;
        }
        c.close();
        return 0.0;
    }

    public void fechar() { db.close(); }
}

