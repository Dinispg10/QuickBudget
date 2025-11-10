package com.example.quickbudget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DespesaAdapter extends RecyclerView.Adapter<DespesaAdapter.VH> {

    // Interface para clique em item
    public interface OnItemClick { void onClick(Despesa despesa); }

    private final List<Despesa> items = new ArrayList<>(); // lista de despesas
    private final OnItemClick onItemClick; // listener de clique

    // Construtor simples
    public DespesaAdapter(List<Despesa> despesas) { this(despesas, null); }

    // Construtor completo com listener
    public DespesaAdapter(List<Despesa> despesas, OnItemClick click) {
        if (despesas != null) items.addAll(despesas);
        this.onItemClick = click;
    }

    // Atualiza lista de despesas
    public void setItems(List<Despesa> novas) {
        items.clear();
        if (novas != null) items.addAll(novas);
    }

    // Cria cada item da lista
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_despesa, parent, false);
        return new VH(v);
    }

    // Liga os dados da despesa aos elementos visuais
    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Despesa d = items.get(position);
        holder.tvDesc.setText(d.getDescricao());
        holder.tvValor.setText(String.format(Locale.getDefault(), "€%.2f", d.getValor()));
        holder.tvCategoria.setText(d.getCategoria());
        holder.tvData.setText(DateUtils.formatDate(d.getTimestamp()));

        // Clique no item
        holder.itemView.setOnClickListener(v -> {
            if (onItemClick != null) onItemClick.onClick(d);
        });
    }

    // Retorna quantidade de itens
    @Override
    public int getItemCount() { return items.size(); }

    // Classe interna que representa um item (ViewHolder)
    static class VH extends RecyclerView.ViewHolder {
        TextView tvDesc, tvValor, tvCategoria, tvData;

        VH(View itemView) {
            super(itemView);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvValor = itemView.findViewById(R.id.tvValor);
            tvCategoria = itemView.findViewById(R.id.tvCategoria);
            tvData = itemView.findViewById(R.id.tvData);
        }
    }
}
