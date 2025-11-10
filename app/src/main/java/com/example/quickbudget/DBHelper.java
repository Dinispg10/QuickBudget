package com.example.quickbudget;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    // Nome e versão da base de dados
    private static final String DATABASE_NAME = "quickbudget.db";
    private static final int DATABASE_VERSION = 1;

    // Campos da tabela DESPESAS
    public static final String TABLE_DESPESAS = "despesas";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_DESCRICAO = "descricao";
    public static final String COLUMN_CATEGORIA = "categoria";
    public static final String COLUMN_VALOR = "valor";
    public static final String COLUMN_RECORRENCIA = "recorrencia";
    public static final String COLUMN_TIMESTAMP = "timestamp"; // data/hora da despesa

    // Campos da tabela BUDGET
    public static final String TABLE_BUDGET = "budget";
    public static final String COLUMN_WEEK_START = "start_of_week"; // início da semana
    public static final String COLUMN_BUDGET_VALUE = "valor"; // valor do orçamento

    // Construtor da base de dados
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Cria as tabelas na primeira execução
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

    // Atualiza base de dados quando muda a versão
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DESPESAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGET);
        onCreate(db);
    }
}
