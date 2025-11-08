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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

        long inicioSemana = DateUtils.getWeekStartMillis();

        DespesaDAO dao = new DespesaDAO(requireContext());
        List<Despesa> despesasSemana = dao.listarSemana(inicioSemana);
        dao.fechar();

        adapter = new DespesaAdapter(despesasSemana, despesa -> {
            DetalheDespesaDialogFragment dialog =
                    DetalheDespesaDialogFragment.nova(despesa.getId());
            dialog.setOnDespesaAlteradaListener(this);
            dialog.show(getParentFragmentManager(), "DetalheDespesa");
        });

        rvWeekExpenses.setAdapter(adapter);
    }


    private void atualizarResumo() {
        long inicioSemana = DateUtils.getWeekStartMillis();
        long fimSemana = DateUtils.getWeekEndMillis();

        DespesaDAO dao = new DespesaDAO(requireContext());
        double total = dao.getTotalPorIntervalo(inicioSemana, fimSemana);
        dao.fechar();

        BudgetDAO bdao = new BudgetDAO(requireContext());
        double budget = bdao.getBudgetPorSemana(inicioSemana);
        bdao.fechar();

        double diff = budget - total;
        double avg = total / 7.0;

        tvWeeklyBudget.setText(String.format(Locale.getDefault(), "Orçamento semanal: €%.2f", budget));
        tvTotalSpent.setText(String.format(Locale.getDefault(), "Total gasto: €%.2f", total));
        tvDifference.setText(String.format(Locale.getDefault(), "Saldo: €%.2f", diff));
        tvAverage.setText(String.format(Locale.getDefault(), "Média diária: €%.2f", avg));

        if (diff < 0) {
            tvDifference.setTextColor(Color.RED);
            tvStatus.setText("Excedeu o orçamento");
            tvStatus.setTextColor(Color.RED);
        } else {
            tvDifference.setTextColor(Color.GREEN);
            tvStatus.setText("Dentro do orçamento");
            tvStatus.setTextColor(Color.GREEN);
        }
    }

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);

        // 🗓️ Labels das últimas 4 semanas ("03 - 10 Nov")
        List<String> semanas = DateUtils.getLastWeeksLabels(4);
        List<BarEntry> gastoEntries = new ArrayList<>();
        List<BarEntry> budgetEntries = new ArrayList<>();

        DespesaDAO ddao = new DespesaDAO(requireContext());
        BudgetDAO bdao = new BudgetDAO(requireContext());

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // 🔄 Gera os valores reais de cada semana (últimas 4)
        for (int i = 3; i >= 0; i--) {
            Calendar start = (Calendar) cal.clone();
            start.add(Calendar.WEEK_OF_YEAR, -i);
            long startMillis = start.getTimeInMillis();
            long endMillis = startMillis + 7L * 24 * 60 * 60 * 1000;

            double totalGasto = ddao.getTotalPorIntervalo(startMillis, endMillis);
            double budget = bdao.getBudgetPorSemana(startMillis);

            gastoEntries.add(new BarEntry(3 - i, (float) totalGasto));
            budgetEntries.add(new BarEntry(3 - i, (float) budget));
        }

        ddao.fechar();
        bdao.fechar();

        // 🎨 Configurações visuais
        BarDataSet setGasto = new BarDataSet(gastoEntries, "Gasto");
        setGasto.setColor(Color.parseColor("#FF7043"));

        BarDataSet setBudget = new BarDataSet(budgetEntries, "Orçamento");
        setBudget.setColor(Color.parseColor("#3FA4CE"));

        BarData data = new BarData(setGasto, setBudget);
        data.setBarWidth(0.35f);

        barChart.setData(data);

        // 📊 Eixo X — usa as labels com intervalo de semana
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(semanas));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setDrawGridLines(false);

        // 📈 Eixo Y
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        barChart.getAxisRight().setEnabled(false);

        // Agrupamento das barras
        float groupSpace = 0.25f, barSpace = 0.02f;
        barChart.getXAxis().setAxisMinimum(0f);
        barChart.getXAxis().setAxisMaximum(data.getGroupWidth(groupSpace, barSpace) * semanas.size());
        barChart.groupBars(0f, groupSpace, barSpace);

        barChart.invalidate();
    }


    @Override
    public void onDespesaAlterada() {
        atualizarResumo();
        setupBarChart();
    }
}
