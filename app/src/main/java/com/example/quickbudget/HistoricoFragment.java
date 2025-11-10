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

/**
 * HistoricoFragment
 * -----------------
 * Fragmento responsável por exibir o histórico financeiro da semana atual
 * e das últimas semanas, através de uma lista e de um gráfico comparativo.
 * Mostra total gasto, orçamento, saldo, média diária e estado (dentro/excedido).
 */
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

        // Inicializa lista, resumo e gráfico
        setupRecyclerView();
        atualizarResumo();
        setupBarChart();

        return view;
    }

    /** Configura o RecyclerView com as despesas da semana atual */
    private void setupRecyclerView() {
        rvWeekExpenses.setHasFixedSize(true);
        rvWeekExpenses.setLayoutManager(new LinearLayoutManager(getContext()));

        long inicioSemana = DateUtils.getWeekStartMillis();

        // Busca despesas da semana
        DespesaDAO dao = new DespesaDAO(requireContext());
        List<Despesa> despesasSemana = dao.listarSemana(inicioSemana);
        dao.fechar();

        // Adapter com ação de clique → abrir detalhe da despesa
        adapter = new DespesaAdapter(despesasSemana, despesa -> {
            DetalheDespesaDialogFragment dialog =
                    DetalheDespesaDialogFragment.nova(despesa.getId());
            dialog.setOnDespesaAlteradaListener(this);
            dialog.show(getParentFragmentManager(), "DetalheDespesa");
        });

        rvWeekExpenses.setAdapter(adapter);
    }

    /** Atualiza o resumo semanal (orçamento, gasto total, saldo, média e estado) */
    private void atualizarResumo() {
        long inicioSemana = DateUtils.getWeekStartMillis();
        long fimSemana = DateUtils.getWeekEndMillis();

        // Calcula total gasto e orçamento
        DespesaDAO dao = new DespesaDAO(requireContext());
        double total = dao.getTotalPorIntervalo(inicioSemana, fimSemana);
        dao.fechar();

        BudgetDAO bdao = new BudgetDAO(requireContext());
        double budget = bdao.getBudgetPorSemana(inicioSemana);
        bdao.fechar();

        double diff = budget - total;
        double avg = total / 7.0;

        // Atualiza textos
        tvWeeklyBudget.setText(String.format(Locale.getDefault(), "Orçamento semanal: €%.2f", budget));
        tvTotalSpent.setText(String.format(Locale.getDefault(), "Total gasto: €%.2f", total));
        tvDifference.setText(String.format(Locale.getDefault(), "Saldo: €%.2f", diff));
        tvAverage.setText(String.format(Locale.getDefault(), "Média diária: €%.2f", avg));

        int negativeColor = ContextCompat.getColor(requireContext(), R.color.history_negative_text);
        int positiveColor = ContextCompat.getColor(requireContext(), R.color.history_positive_text);

        // Define cor e mensagem consoante o saldo
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

    /** Configura o gráfico de barras com os dados das últimas 4 semanas */
    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);

        // Labels (ex: "03–09 Nov", "10–16 Nov", etc.)
        List<String> semanas = DateUtils.getLastWeeksLabels(4);
        List<BarEntry> gastoEntries = new ArrayList<>();
        List<BarEntry> budgetEntries = new ArrayList<>();

        DespesaDAO ddao = new DespesaDAO(requireContext());
        BudgetDAO bdao = new BudgetDAO(requireContext());

        // Preenche dados de cada semana
        for (int i = 3; i >= 0; i--) {
            long[] range = DateUtils.getWeekRangeFromNowOffset(-i);
            double totalGasto = ddao.getTotalPorIntervalo(range[0], range[1]);
            double budget = bdao.getBudgetPorSemana(range[0]);
            gastoEntries.add(new BarEntry(3 - i, (float) totalGasto));
            budgetEntries.add(new BarEntry(3 - i, (float) budget));
        }

        ddao.fechar();
        bdao.fechar();

        // Conjuntos de dados
        BarDataSet setGasto = new BarDataSet(gastoEntries, "Gasto");
        setGasto.setColor(Color.parseColor("#FF7043"));
        setGasto.setValueTextSize(12f);
        setGasto.setValueTextColor(Color.BLACK);

        BarDataSet setBudget = new BarDataSet(budgetEntries, "Orçamento");
        setBudget.setColor(Color.parseColor("#3FA4CE"));
        setBudget.setValueTextSize(12f);
        setBudget.setValueTextColor(Color.BLACK);

        BarData data = new BarData(setGasto, setBudget);
        data.setBarWidth(0.35f);
        barChart.setData(data);

        // Eixo X (semanas)
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(semanas));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(12f);
        xAxis.setTextColor(Color.BLACK);

        // Eixo Y (valores)
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextSize(12f);
        leftAxis.setTextColor(Color.BLACK);
        barChart.getAxisRight().setEnabled(false);

        // Agrupamento e animação
        float groupSpace = 0.25f, barSpace = 0.02f;
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(data.getGroupWidth(groupSpace, barSpace) * semanas.size());
        barChart.groupBars(0f, groupSpace, barSpace);
        barChart.animateY(1000, Easing.EaseInOutQuad);

        // Legenda
        Legend legend = barChart.getLegend();
        legend.setTextColor(Color.BLACK);
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

    /** Atualiza lista, resumo e gráfico após editar uma despesa */
    @Override
    public void onDespesaAlterada() {
        setupRecyclerView();
        atualizarResumo();
        setupBarChart();
    }
}
