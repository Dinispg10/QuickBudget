package com.example.quickbudget;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoricoFragment extends Fragment implements DetalheDespesaDialogFragment.OnDespesaAlteradaListener {

    private TextView tvWeeklyBudget, tvTotalSpent, tvDifference, tvAverage, tvSummaryTitle, tvStatus;
    private RecyclerView rvWeekExpenses;
    private BarChart barChart;
    private DespesaAdapter adapter;

    public HistoricoFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        tvSummaryTitle = view.findViewById(R.id.textViewHistorySummary);
        tvWeeklyBudget = view.findViewById(R.id.textViewWeeklyBudget);
        tvTotalSpent = view.findViewById(R.id.textViewTotalSpent);
        tvDifference = view.findViewById(R.id.textViewDifference);
        tvAverage = view.findViewById(R.id.textViewAverage);
        tvStatus = view.findViewById(R.id.textViewStatus);

        rvWeekExpenses = view.findViewById(R.id.recyclerViewWeekExpenses);
        barChart = view.findViewById(R.id.barChartWeeks);

        tvSummaryTitle.setText("Resumo da Semana (" + DateUtils.getCurrentWeekRangeString() + ")");

        setupRecyclerView();
        atualizarResumo();
        setupBarChart();

        return view;
    }

    private void setupRecyclerView() {
        rvWeekExpenses.setHasFixedSize(true);
        rvWeekExpenses.setLayoutManager(new LinearLayoutManager(getContext()));

        // ✅ Agora a lista permite abrir e editar despesas
        adapter = new DespesaAdapter(
                DespesaStorage.getDespesasSemana(requireContext()),
                despesa -> {
                    DetalheDespesaDialogFragment dialog = DetalheDespesaDialogFragment.nova(despesa.getId());
                    dialog.setOnDespesaAlteradaListener(this);
                    dialog.show(getParentFragmentManager(), "DetalheDespesa");
                }
        );

        rvWeekExpenses.setAdapter(adapter);
    }

    private void atualizarResumo() {
        double budget = BudgetStorage.getWeeklyBudget(requireContext());
        double total = DespesaStorage.getTotalGastoSemana(requireContext());
        double diff = budget - total;
        double avg = total / 7.0;

        tvWeeklyBudget.setText(String.format(Locale.getDefault(), "Orçamento semanal: €%.2f", budget));
        tvTotalSpent.setText(String.format(Locale.getDefault(), "Total gasto: €%.2f", total));
        tvDifference.setText(String.format(Locale.getDefault(), "Saldo: €%.2f", diff));
        tvAverage.setText(String.format(Locale.getDefault(), "Média diária: €%.2f", avg));

        if (diff < 0) {
            tvDifference.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            tvStatus.setText("Fora do orçamento");
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_light));
        } else {
            tvDifference.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            tvStatus.setText("Dentro do orçamento");
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
    }

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setPinchZoom(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setNoDataText("Sem dados suficientes para mostrar o gráfico");

        List<String> semanas = DateUtils.getLastWeeksLabels(4);
        List<Double> gastos = DespesaStorage.getTotalGastosSemanas(requireContext(), 4);
        List<Double> budgets = BudgetStorage.getBudgetsUltimasSemanas(requireContext(), 4);

        List<BarEntry> gastoEntries = new ArrayList<>();
        List<BarEntry> budgetEntries = new ArrayList<>();

        for (int i = 0; i < semanas.size(); i++) {
            gastoEntries.add(new BarEntry(i, gastos.get(i).floatValue()));
            budgetEntries.add(new BarEntry(i, budgets.get(i).floatValue()));
        }

        BarDataSet setGasto = new BarDataSet(gastoEntries, "Gasto");
        setGasto.setColor(Color.parseColor("#FF7043")); // Laranja

        BarDataSet setBudget = new BarDataSet(budgetEntries, "Orçamento");
        setBudget.setColor(Color.parseColor("#3FA4CE")); // Azul pastel

        BarData data = new BarData(setGasto, setBudget);
        data.setValueTextSize(12f);

        // ✅ Corrige o agrupamento das barras
        float groupSpace = 0.25f; // espaçamento entre grupos
        float barSpace = 0.02f;   // espaço entre barras do mesmo grupo
        float barWidth = 0.35f;   // largura de cada barra
        data.setBarWidth(barWidth);

        barChart.setData(data);

        // Eixo X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(semanas));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true); // ✅ necessário para agrupamento
        xAxis.setDrawGridLines(false);

        // Eixo Y
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);

        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(true);

        // ⚙️ MUITO IMPORTANTE: definir limites corretos para agrupar
        barChart.setVisibleXRangeMaximum(semanas.size());
        barChart.getXAxis().setAxisMinimum(0f);
        barChart.getXAxis().setAxisMaximum(0f + data.getGroupWidth(groupSpace, barSpace) * semanas.size());

        barChart.groupBars(0f, groupSpace, barSpace);
        barChart.invalidate();
    }


    @Override
    public void onDespesaAlterada() {
        adapter.setItems(DespesaStorage.getDespesasSemana(requireContext()));
        adapter.notifyDataSetChanged();
        atualizarResumo();
        setupBarChart();
    }
}
