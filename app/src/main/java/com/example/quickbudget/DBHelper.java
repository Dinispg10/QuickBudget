package com.example.quickbudget;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "quickbudget.db";
    private static final int DATABASE_VERSION = 1;

    // ===== TABELA DESPESAS =====
    public static final String TABLE_DESPESAS = "despesas";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_DESCRICAO = "descricao";
    public static final String COLUMN_CATEGORIA = "categoria";
    public static final String COLUMN_VALOR = "valor";
    public static final String COLUMN_RECORRENCIA = "recorrencia";
    public static final String COLUMN_TIMESTAMP = "timestamp"; // data/hora da despesa

    // ===== TABELA BUDGET =====
    public static final String TABLE_BUDGET = "budget";
    public static final String COLUMN_WEEK_START = "start_of_week"; // segunda-feira 00h00
    public static final String COLUMN_BUDGET_VALUE = "valor"; // valor do orçamento semanal

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Criação da tabela DESPESAS
        db.execSQL("CREATE TABLE " + TABLE_DESPESAS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DESCRICAO + " TEXT NOT NULL, " +
                COLUMN_CATEGORIA + " TEXT, " +
                COLUMN_VALOR + " REAL, " +
                COLUMN_RECORRENCIA + " TEXT, " +
                COLUMN_TIMESTAMP + " INTEGER)");

        // Criação da tabela BUDGET
        db.execSQL("CREATE TABLE " + TABLE_BUDGET + " (" +
                COLUMN_WEEK_START + " INTEGER PRIMARY KEY, " +
                COLUMN_BUDGET_VALUE + " REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DESPESAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGET);
        onCreate(db);
    }
}
