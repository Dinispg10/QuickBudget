package com.example.quickbudget;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DBHelper
 * ---------
 * Classe responsável pela criação e gestão da base de dados local (SQLite).
 * Contém a definição das tabelas:
 *  - DESPESAS: armazena as despesas individuais registadas pelo utilizador.
 *  - BUDGET: guarda o valor do orçamento semanal por data de início da semana.
 *
 * Gere automaticamente a criação e atualização do esquema da base de dados.
 */
public class DBHelper extends SQLiteOpenHelper {

    // Nome e versão da base de dados
    private static final String DATABASE_NAME = "quickbudget.db";
    private static final int DATABASE_VERSION = 1;

    // ======== TABELA DESPESAS ========
    public static final String TABLE_DESPESAS = "despesas";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_DESCRICAO = "descricao";
    public static final String COLUMN_CATEGORIA = "categoria";
    public static final String COLUMN_VALOR = "valor";
    public static final String COLUMN_RECORRENCIA = "recorrencia";
    public static final String COLUMN_TIMESTAMP = "timestamp"; // Data/hora da despesa

    // ======== TABELA BUDGET ========
    public static final String TABLE_BUDGET = "budget";
    public static final String COLUMN_WEEK_START = "start_of_week"; // Segunda-feira (00:00)
    public static final String COLUMN_BUDGET_VALUE = "valor";       // Valor do orçamento semanal

    /**
     * Construtor do DBHelper.
     * Cria (ou abre) a base de dados para leitura e escrita.
     */
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Executado na primeira vez que a base de dados é criada.
     * Responsável por definir o esquema inicial (tabelas e colunas).
     */
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

    /**
     * Chamado automaticamente quando há uma atualização de versão da base de dados.
     * Elimina as tabelas antigas e recria-as (útil em alterações estruturais).
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DESPESAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGET);
        onCreate(db);
    }
}
