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
    public interface OnItemClick { void onClick(Despesa despesa); }
    private final List<Despesa> items = new ArrayList<>();
    private final OnItemClick onItemClick;

    public DespesaAdapter(List<Despesa> despesas) { this(despesas, null); }
    public DespesaAdapter(List<Despesa> despesas, OnItemClick click) {
        if (despesas != null) items.addAll(despesas);
        this.onItemClick = click;
    }
    public void setItems(List<Despesa> novas) {
        items.clear();
        if (novas != null) items.addAll(novas);
    }
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new VH(v);
    }
    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Despesa d = items.get(position);
        holder.tvDesc.setText(d.getDescricao());
        holder.tvValor.setText(String.format(Locale.getDefault(), "€%.2f", d.getValor()));
        holder.tvCategoria.setText(d.getCategoria());
        holder.tvData.setText(DateUtils.formatDate(d.getTimestamp()));
        holder.itemView.setOnClickListener(v -> {
            if (onItemClick != null) onItemClick.onClick(d);
        });
    }
    @Override public int getItemCount() { return items.size(); }
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
