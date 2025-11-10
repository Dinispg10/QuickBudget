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
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.animation.Easing;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoricoFragment extends Fragment implements DetalheDespesaDialogFragment.OnDespesaAlteradaListener {

    private RecyclerView rvWeekExpenses;
    private TextView tvWeeklyBudget, tvTotalSpent, tvDifference, tvAverage, tvSummaryTitle, tvStatus;
    private BarChart barChart;
    private DespesaAdapter adapter;

    public HistoricoFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_historico, container, false);

        // Liga elementos do layout
        tvSummaryTitle = view.findViewById(R.id.textViewHistorySummary);
        tvWeeklyBudget = view.findViewById(R.id.textViewWeeklyBudget);
        tvTotalSpent = view.findViewById(R.id.textViewTotalSpent);
        tvDifference = view.findViewById(R.id.textViewDifference);
        tvAverage = view.findViewById(R.id.textViewAverage);
        tvStatus = view.findViewById(R.id.textViewStatus);
        rvWeekExpenses = view.findViewById(R.id.recyclerViewWeekExpenses);
        barChart = view.findViewById(R.id.barChartWeeks);

        // Mostra o intervalo da semana atual
        tvSummaryTitle.setText("Resumo da Semana (" + DateUtils.getCurrentWeekRangeString() + ")");

        // Configura lista, gráfico e resumo
        setupRecyclerView();
        atualizarResumo();
        setupBarChart();

        return view;
    }

    // Configura a RecyclerView
    private void setupRecyclerView() {
        rvWeekExpenses.setHasFixedSize(true);
        rvWeekExpenses.setLayoutManager(new LinearLayoutManager(getContext()));

        long inicioSemana = DateUtils.getWeekStartMillis();

        // Busca despesas da semana
        DespesaDAO dao = new DespesaDAO(requireContext());
        List<Despesa> despesasSemana = dao.listarSemana(inicioSemana);
        dao.fechar();

        // Define o adaptador
        adapter = new DespesaAdapter(despesasSemana, despesa -> {
            // Abre o diálogo de detalhe ao clicar
            DetalheDespesaDialogFragment dialog =
                    DetalheDespesaDialogFragment.nova(despesa.getId());
            dialog.setOnDespesaAlteradaListener(this);
            dialog.show(getParentFragmentManager(), "DetalheDespesa");
        });

        rvWeekExpenses.setAdapter(adapter);
    }

    // Atualiza resumo semanal
    private void atualizarResumo() {
        long inicioSemana = DateUtils.getWeekStartMillis();
        long fimSemana = DateUtils.getWeekEndMillis();

        // Calcula totais
        DespesaDAO dao = new DespesaDAO(requireContext());
        double total = dao.getTotalPorIntervalo(inicioSemana, fimSemana);
        dao.fechar();

        BudgetDAO bdao = new BudgetDAO(requireContext());
        double budget = bdao.getBudgetPorSemana(inicioSemana);
        bdao.fechar();

        double diff = budget - total;
        double avg = total / 7.0;

        // Mostra os valores no ecrã
        tvWeeklyBudget.setText(String.format(Locale.getDefault(), "Orçamento semanal: €%.2f", budget));
        tvTotalSpent.setText(String.format(Locale.getDefault(), "Total gasto: €%.2f", total));
        tvDifference.setText(String.format(Locale.getDefault(), "Saldo: €%.2f", diff));
        tvAverage.setText(String.format(Locale.getDefault(), "Média diária: €%.2f", avg));

        int negativeColor = ContextCompat.getColor(requireContext(), R.color.history_negative_text);
        int positiveColor = ContextCompat.getColor(requireContext(), R.color.history_positive_text);

        // Muda cores conforme resultado
        if (diff < 0) {
            tvDifference.setTextColor(negativeColor);
            tvStatus.setText("Excedeu o orçamento");
            tvStatus.setTextColor(negativeColor);
        } else {
            tvDifference.setTextColor(positiveColor);
            tvStatus.setText("Dentro do orçamento");
            tvStatus.setTextColor(positiveColor);
        }
    }

    // Configura gráfico de barras
    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);

        // Labels das últimas 4 semanas
        List<String> semanas = DateUtils.getLastWeeksLabels(4);
        List<BarEntry> gastoEntries = new ArrayList<>();
        List<BarEntry> budgetEntries = new ArrayList<>();

        DespesaDAO ddao = new DespesaDAO(requireContext());
        BudgetDAO bdao = new BudgetDAO(requireContext());

        // Adiciona dados semana a semana
        for (int i = 3; i >= 0; i--) {
            long[] range = DateUtils.getWeekRangeFromNowOffset(-i);
            long startMillis = range[0];
            long endMillis = range[1];

            double totalGasto = ddao.getTotalPorIntervalo(startMillis, endMillis);
            double budget = bdao.getBudgetPorSemana(startMillis);

            gastoEntries.add(new BarEntry(3 - i, (float) totalGasto));
            budgetEntries.add(new BarEntry(3 - i, (float) budget));
        }

        ddao.fechar();
        bdao.fechar();

        // Cria conjuntos de dados
        BarDataSet setGasto = new BarDataSet(gastoEntries, "Gasto");
        setGasto.setColor(Color.parseColor("#FF7043"));
        setGasto.setValueTextSize(12f);
        setGasto.setValueTextColor(Color.BLACK); // texto preto

        BarDataSet setBudget = new BarDataSet(budgetEntries, "Orçamento");
        setBudget.setColor(Color.parseColor("#3FA4CE"));
        setBudget.setValueTextSize(12f);
        setBudget.setValueTextColor(Color.BLACK); // texto preto

        BarData data = new BarData(setGasto, setBudget);
        data.setBarWidth(0.35f);
        barChart.setData(data);

// Eixo X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(semanas));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(12f);
        xAxis.setTextColor(Color.BLACK); // texto preto

// Eixo Y
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextSize(12f);
        leftAxis.setTextColor(Color.BLACK); // texto preto
        barChart.getAxisRight().setEnabled(false);

// Agrupa barras e adiciona animação
        float groupSpace = 0.25f, barSpace = 0.02f;
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(data.getGroupWidth(groupSpace, barSpace) * semanas.size());
        barChart.groupBars(0f, groupSpace, barSpace);
        barChart.animateY(1000, Easing.EaseInOutQuad); // animação suave

// Legenda do gráfico
        Legend legend = barChart.getLegend();
        legend.setTextColor(Color.BLACK); // texto preto
        legend.setTextSize(12f);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setXEntrySpace(10f);
        legend.setYEntrySpace(5f);

        barChart.invalidate();
    }

    // Atualiza tudo ao editar despesa
    @Override
    public void onDespesaAlterada() {
        setupRecyclerView();
        atualizarResumo();
        setupBarChart();
    }
}
