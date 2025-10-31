package com.example.quickbudget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Representa uma despesa individual.
 * Compatível com o sistema de armazenamento em JSON e recorrência automática.
 */
public class Despesa {
    private String id;
    private String descricao;
    private double valor;
    private String categoria;
    private String recorrencia; // Nenhuma, Semanal, Mensal
    private long timestamp; // data/hora da despesa

    // Construtor principal (usado ao criar nova despesa)
    public Despesa(String descricao, String categoria, double valor, long timestamp, String recorrencia) {
        this.id = UUID.randomUUID().toString();
        this.descricao = descricao;
        this.categoria = categoria;
        this.valor = valor;
        this.timestamp = timestamp;
        this.recorrencia = recorrencia;
    }
    // Construtor completo (usado ao carregar despesas existentes)
    public Despesa(String id, String descricao, String categoria, double valor, long timestamp, String recorrencia) {
        this.id = id;
        this.descricao = descricao;
        this.categoria = categoria;
        this.valor = valor;
        this.timestamp = timestamp;
        this.recorrencia = recorrencia;
    }

    // Getters e Setters
    public String getId() { return id; }
    public String getDescricao() { return descricao; }
    public double getValor() { return valor; }
    public String getCategoria() { return categoria; }
    public String getRecorrencia() { return recorrencia; }
    public long getTimestamp() { return timestamp; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setValor(double valor) { this.valor = valor; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setRecorrencia(String recorrencia) { this.recorrencia = recorrencia; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    // Conversão para e de JSON (para armazenar no SharedPreferences)
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", id);
            obj.put("descricao", descricao);
            obj.put("categoria", categoria);
            obj.put("valor", valor);
            obj.put("recorrencia", recorrencia);
            obj.put("timestamp", timestamp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }
    public static Despesa fromJson(JSONObject obj) {
        try {
            return new Despesa(
                    obj.getString("id"),
                    obj.getString("descricao"),
                    obj.getString("categoria"),
                    obj.getDouble("valor"),
                    obj.getLong("timestamp"),
                    obj.optString("recorrencia", "Nenhuma")
            );
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String toJson(List<Despesa> lista) {
        JSONArray arr = new JSONArray();
        for (Despesa d : lista) arr.put(d.toJson());
        return arr.toString();
    }
    public static List<Despesa> fromJsonList(String json) {
        List<Despesa> lista = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                Despesa d = fromJson(obj);
                if (d != null) lista.add(d);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
