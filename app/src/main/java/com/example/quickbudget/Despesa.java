package com.example.quickbudget;

public class Despesa {

    // Campos da despesa
    private int id;                 // ID autoincrementado no SQLite
    private String descricao;       // texto descritivo
    private double valor;           // valor da despesa
    private String categoria;       // categoria (alimentação, transporte, etc.)
    private String recorrencia;     // tipo de recorrência
    private long timestamp;         // data/hora da despesa

    // Construtor usado para nova despesa (sem ID)
    public Despesa(String descricao, String categoria, double valor, long timestamp, String recorrencia) {
        this.descricao = descricao;
        this.categoria = categoria;
        this.valor = valor;
        this.timestamp = timestamp;
        this.recorrencia = recorrencia;
    }

    // Construtor completo (com ID)
    public Despesa(int id, String descricao, String categoria, double valor, long timestamp, String recorrencia) {
        this.id = id;
        this.descricao = descricao;
        this.categoria = categoria;
        this.valor = valor;
        this.timestamp = timestamp;
        this.recorrencia = recorrencia;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getRecorrencia() { return recorrencia; }
    public void setRecorrencia(String recorrencia) { this.recorrencia = recorrencia; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
