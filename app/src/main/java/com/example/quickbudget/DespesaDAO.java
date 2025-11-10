package com.example.quickbudget;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DespesaDAO {
    private final DBHelper dbHelper; // ligação à base de dados

    // Construtor
    public DespesaDAO(Context context) {
        dbHelper = new DBHelper(context);
    }

    // Inserir nova despesa
    public long inserir(Despesa despesa) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_DESCRICAO, despesa.getDescricao());
        values.put(DBHelper.COLUMN_CATEGORIA, despesa.getCategoria());
        values.put(DBHelper.COLUMN_VALOR, despesa.getValor());
        values.put(DBHelper.COLUMN_RECORRENCIA, despesa.getRecorrencia());
        values.put(DBHelper.COLUMN_TIMESTAMP, despesa.getTimestamp());
        long id = db.insert(DBHelper.TABLE_DESPESAS, null, values);
        db.close();
        return id;
    }

    // Obter despesa por ID
    public Despesa obterPorId(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DBHelper.TABLE_DESPESAS, null,
                DBHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        Despesa d = null;
        if (c.moveToFirst()) d = fromCursor(c);
        c.close();
        db.close();
        return d;
    }

    // Listar todas as despesas
    public List<Despesa> listarTodas() {
        List<Despesa> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DBHelper.TABLE_DESPESAS, null, null, null, null, null,
                DBHelper.COLUMN_TIMESTAMP + " DESC");

        while (c.moveToNext()) lista.add(fromCursor(c));
        c.close();
        db.close();
        return lista;
    }

    // Listar despesas da semana
    public List<Despesa> listarSemana(long inicioSemana) {
        List<Despesa> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        long fimSemana = DateUtils.getWeekEndMillis();

        Cursor c = db.query(
                DBHelper.TABLE_DESPESAS,
                null,
                DBHelper.COLUMN_TIMESTAMP + " BETWEEN ? AND ?",
                new String[]{String.valueOf(inicioSemana), String.valueOf(fimSemana)},
                null, null,
                DBHelper.COLUMN_TIMESTAMP + " DESC"
        );

        if (c != null) {
            while (c.moveToNext()) lista.add(fromCursor(c));
            c.close();
        }

        db.close();
        return lista;
    }

    // Calcular total gasto num intervalo
    public double getTotalPorIntervalo(long inicio, long fim) {
        double total = 0.0;
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT SUM(" + DBHelper.COLUMN_VALOR + ") AS total FROM " + DBHelper.TABLE_DESPESAS +
                        " WHERE " + DBHelper.COLUMN_TIMESTAMP + " BETWEEN ? AND ?",
                new String[]{String.valueOf(inicio), String.valueOf(fim)}
        );

        if (c.moveToFirst()) total = c.getDouble(c.getColumnIndexOrThrow("total"));
        c.close();
        db.close();
        return total;
    }

    // Atualizar uma despesa existente
    public void atualizar(Despesa despesa) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_DESCRICAO, despesa.getDescricao());
        values.put(DBHelper.COLUMN_CATEGORIA, despesa.getCategoria());
        values.put(DBHelper.COLUMN_VALOR, despesa.getValor());
        values.put(DBHelper.COLUMN_RECORRENCIA, despesa.getRecorrencia());
        values.put(DBHelper.COLUMN_TIMESTAMP, despesa.getTimestamp());

        db.update(DBHelper.TABLE_DESPESAS, values,
                DBHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(despesa.getId())});
        db.close();
    }

    // Eliminar despesa pelo ID
    public void eliminar(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DBHelper.TABLE_DESPESAS,
                DBHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    // Verifica se já existe despesa semelhante na mesma semana
    private boolean existeDespesaSimilar(String descricao, String categoria, double valor, long inicioSemana) {
        long fimSemana = DateUtils.getWeekEndMillis();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.query(DBHelper.TABLE_DESPESAS,
                null,
                DBHelper.COLUMN_DESCRICAO + "=? AND " +
                        DBHelper.COLUMN_CATEGORIA + "=? AND " +
                        DBHelper.COLUMN_VALOR + "=? AND " +
                        DBHelper.COLUMN_TIMESTAMP + " BETWEEN ? AND ?",
                new String[]{
                        descricao,
                        categoria,
                        String.valueOf(valor),
                        String.valueOf(inicioSemana),
                        String.valueOf(fimSemana)
                },
                null, null, null);

        boolean existe = (c != null && c.moveToFirst());
        if (c != null) c.close();
        return existe;
    }

    // Gera automaticamente despesas recorrentes
    public void gerarDespesasRecorrentes(long inicioSemana) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try (Cursor c = db.query(DBHelper.TABLE_DESPESAS, null,
                DBHelper.COLUMN_RECORRENCIA + " IN (?, ?)",
                new String[]{"Semanal", "Mensal"}, null, null, null)) {

            if (c != null) {
                while (c.moveToNext()) {
                    String descricao = c.getString(c.getColumnIndexOrThrow(DBHelper.COLUMN_DESCRICAO));
                    String categoria = c.getString(c.getColumnIndexOrThrow(DBHelper.COLUMN_CATEGORIA));
                    double valor = c.getDouble(c.getColumnIndexOrThrow(DBHelper.COLUMN_VALOR));
                    String recorrencia = c.getString(c.getColumnIndexOrThrow(DBHelper.COLUMN_RECORRENCIA));
                    long timestampAntigo = c.getLong(c.getColumnIndexOrThrow(DBHelper.COLUMN_TIMESTAMP));

                    boolean deveCriar = false;

                    // Verifica se deve criar despesa nova conforme recorrência
                    if ("Semanal".equalsIgnoreCase(recorrencia)) {
                        if (timestampAntigo < inicioSemana) deveCriar = true;
                    } else if ("Mensal".equalsIgnoreCase(recorrencia)) {
                        Calendar calUltima = Calendar.getInstance();
                        calUltima.setTimeInMillis(timestampAntigo);
                        Calendar calAgora = Calendar.getInstance();

                        if (calAgora.get(Calendar.MONTH) != calUltima.get(Calendar.MONTH)
                                || calAgora.get(Calendar.YEAR) != calUltima.get(Calendar.YEAR)) {
                            deveCriar = true;
                        }
                    }

                    // Cria nova despesa se não existir igual
                    if (deveCriar && !existeDespesaSimilar(descricao, categoria, valor, inicioSemana)) {
                        ContentValues values = new ContentValues();
                        values.put(DBHelper.COLUMN_DESCRICAO, descricao);
                        values.put(DBHelper.COLUMN_CATEGORIA, categoria);
                        values.put(DBHelper.COLUMN_VALOR, valor);
                        values.put(DBHelper.COLUMN_RECORRENCIA, recorrencia);
                        values.put(DBHelper.COLUMN_TIMESTAMP, System.currentTimeMillis());
                        db.insert(DBHelper.TABLE_DESPESAS, null, values);
                    }
                }
            }
        }

        db.close();
    }

    // Cria objeto Despesa a partir do cursor
    private Despesa fromCursor(Cursor c) {
        return new Despesa(
                c.getInt(c.getColumnIndexOrThrow(DBHelper.COLUMN_ID)),
                c.getString(c.getColumnIndexOrThrow(DBHelper.COLUMN_DESCRICAO)),
                c.getString(c.getColumnIndexOrThrow(DBHelper.COLUMN_CATEGORIA)),
                c.getDouble(c.getColumnIndexOrThrow(DBHelper.COLUMN_VALOR)),
                c.getLong(c.getColumnIndexOrThrow(DBHelper.COLUMN_TIMESTAMP)),
                c.getString(c.getColumnIndexOrThrow(DBHelper.COLUMN_RECORRENCIA))
        );
    }

    // Fecha o helper da base de dados
    public void fechar() {
        dbHelper.close();
    }
}
