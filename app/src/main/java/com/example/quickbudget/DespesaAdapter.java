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

/**
 * DespesaAdapter
 * ---------------
 * Adapter responsável por exibir a lista de despesas num RecyclerView.
 * Cada item mostra a descrição, categoria, valor e data da despesa.
 * Permite interação através de um listener de clique (para abrir/editar detalhes).
 */
public class DespesaAdapter extends RecyclerView.Adapter<DespesaAdapter.VH> {

    // Interface de callback para cliques em despesas
    public interface OnItemClick {
        void onClick(Despesa despesa);
    }

    private final List<Despesa> items = new ArrayList<>();
    private final OnItemClick onItemClick;

    // Construtor principal
    public DespesaAdapter(List<Despesa> despesas, OnItemClick click) {
        if (despesas != null) items.addAll(despesas);
        this.onItemClick = click;
    }

    // Substitui a lista de despesas por uma nova
    public void setItems(List<Despesa> novas) {
        items.clear();
        if (novas != null) items.addAll(novas);
    }

    // Cria a estrutura visual (ViewHolder) para cada item
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_despesa, parent, false);
        return new VH(v);
    }

    // Liga os dados da despesa aos elementos visuais do layout
    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Despesa d = items.get(position);

        holder.tvDesc.setText(d.getDescricao());
        holder.tvValor.setText(String.format(Locale.getDefault(), "€%.2f", d.getValor()));
        holder.tvCategoria.setText(d.getCategoria());
        holder.tvData.setText(DateUtils.formatDate(d.getTimestamp()));

        // Listener para clique no item → abre detalhe
        holder.itemView.setOnClickListener(v -> {
            if (onItemClick != null) onItemClick.onClick(d);
        });
    }

    // Retorna o número total de despesas exibidas
    @Override
    public int getItemCount() {
        return items.size();
    }

    // Classe ViewHolder que contém as views de um item da lista
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
