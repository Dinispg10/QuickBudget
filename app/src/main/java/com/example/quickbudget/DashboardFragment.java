package com.example.quickbudget;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardFragment extends Fragment implements DetalheDespesaDialogFragment.OnDespesaAlteradaListener {

    private RecyclerView rvRecentExpenses;
    private DespesaAdapter adapter;
    private PieChart pieChart;
    private TextView tvWeekSummary, tvProgressDetail, tvTotalSpent, tvWeeklyBudget, tvBudgetRemaining, tvLeftOf, tvAvgDay;
    private ProgressBar progressBar;
    private EditText editNewBudget;
    private Button buttonUpdateBudget;

    public DashboardFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        rvRecentExpenses = view.findViewById(R.id.recyclerViewRecentExpenses);
        pieChart = view.findViewById(R.id.pieChartCategories);
        tvWeekSummary = view.findViewById(R.id.textViewWeekSummary);
        tvTotalSpent = view.findViewById(R.id.textViewTotalSpent);
        tvWeeklyBudget = view.findViewById(R.id.textViewWeeklyBudget);
        tvProgressDetail = view.findViewById(R.id.textViewProgressDetail);
        progressBar = view.findViewById(R.id.progressBarBudget);
        editNewBudget = view.findViewById(R.id.editTextNewBudget);
        buttonUpdateBudget = view.findViewById(R.id.buttonUpdateBudget);
        tvBudgetRemaining = view.findViewById(R.id.textViewBudgetRemaining);
        tvLeftOf = view.findViewById(R.id.textViewLeftOf);
        tvAvgDay = view.findViewById(R.id.textViewAvgDay);

        tvWeekSummary.setText("Resumo da semana (" + DateUtils.getCurrentWeekRangeString() + ")");

        // 🔘 Botão de saída
        ImageButton buttonExit = view.findViewById(R.id.buttonExit);
        buttonExit.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle("Sair")
                .setMessage("Tens a certeza que queres sair da aplicação?")
                .setNegativeButton("Não", null)
                .setPositiveButton("Sim", (dialog, which) -> requireActivity().finishAffinity())
                .show());

        setupRecyclerView();
        setupPieChart();
        setupBudgetUpdate();

        refreshAll();

        return view;
    }

    // ===================================
    // 🧾 RecyclerView - Despesas recentes
    // ===================================


    private void atualizarDespesasRecentes() {
        DespesaDAO dao = new DespesaDAO(requireContext());
        long inicioSemana = DateUtils.getWeekStartMillis();
        long fimSemana = DateUtils.getWeekEndMillis();

        List<Despesa> todas = dao.listarTodas();
        dao.fechar();

        // 🔹 Apenas as despesas da semana atual
        List<Despesa> semana = new ArrayList<>();
        for (Despesa d : todas) {
            if (d.getTimestamp() >= inicioSemana && d.getTimestamp() <= fimSemana) {
                semana.add(d);
            }
        }

        // 🔹 Ordenar por data (mais recentes primeiro)
        semana.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

        // 🔹 Mostrar no máximo 2
        List<Despesa> recentes = semana.size() > 2 ? semana.subList(0, 2) : semana;

        if (adapter == null) {
            adapter = new DespesaAdapter(recentes, despesa -> {
                DetalheDespesaDialogFragment dialog =
                        DetalheDespesaDialogFragment.nova(despesa.getId());
                dialog.setOnDespesaAlteradaListener(this);
                dialog.show(getParentFragmentManager(), "DetalheDespesa");
            });
        } else {
            adapter.setItems(recentes);
            adapter.notifyDataSetChanged();
        }
        if (rvRecentExpenses.getAdapter() != adapter) {
            rvRecentExpenses.setAdapter(adapter);
        }


    }

    private void setupRecyclerView() {
        rvRecentExpenses.setHasFixedSize(true);
        rvRecentExpenses.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    // ===================================
    // 💰 Atualizar orçamento semanal
    // ===================================
    private void setupBudgetUpdate() {
        buttonUpdateBudget.setOnClickListener(v -> {
            String valueStr = editNewBudget.getText().toString().trim();
            if (TextUtils.isEmpty(valueStr)) {
                editNewBudget.setError("Insere um novo orçamento!");
                return;
            }
            try {
                double valor = Double.parseDouble(valueStr.replace(",", "."));
                long inicioSemana = DateUtils.getWeekStartMillis();

                BudgetDAO bdao = new BudgetDAO(requireContext());
                bdao.setBudget(valor, inicioSemana);
                bdao.fechar();

                refreshAll();
                editNewBudget.setText("");
            } catch (NumberFormatException ignored) {
                editNewBudget.setError("Valor de orçamento inválido!");
            }
        });
    }

    // ===================================
    // 🥧 Gráfico de pizza
    // ===================================
    private void setupPieChart() {
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(true);

        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
    }

    // ===================================
    // 🔄 Atualizar tudo
    // ===================================
    public void refreshAll() {
        // === Carregar todas as despesas ===
        DespesaDAO ddao = new DespesaDAO(requireContext());
        List<Despesa> despesas = ddao.listarTodas();
        ddao.fechar();

        double total = 0.0;
        Map<String, Double> gastosPorCategoria = new HashMap<>();

        // 🔹 Calcular intervalo da semana atual
        long inicioSemana = DateUtils.getWeekStartMillis();
        long fimSemana = DateUtils.getWeekEndMillis();

        List<Despesa> semana = new ArrayList<>();
        for (Despesa d : despesas) {
            if (d.getTimestamp() >= inicioSemana && d.getTimestamp() <= fimSemana) {
                semana.add(d);
                total += d.getValor();
                gastosPorCategoria.put(
                        d.getCategoria(),
                        gastosPorCategoria.getOrDefault(d.getCategoria(), 0.0) + d.getValor()
                );
            }
        }

        // 🔹 Atualizar lista de despesas recentes
        atualizarDespesasRecentes();

        // 🔹 Ler o orçamento atual (sem criar novo)
        BudgetDAO bdao = new BudgetDAO(requireContext());
        double budget = bdao.getBudgetPorSemana(inicioSemana);
        bdao.fechar();

        double restante = budget - total;
        double mediaPorDia = total / 7.0;

        // === Atualizar UI ===
        tvAvgDay.setText(String.format(Locale.getDefault(), "€%.2f", mediaPorDia));
        tvWeeklyBudget.setText(String.format("Orçamento semanal atual: €%.2f", budget));
        tvTotalSpent.setText(String.format("€%.2f", total));
        tvBudgetRemaining.setText(String.format("€%.2f", restante));

        if (restante < 0) {
            tvBudgetRemaining.setTextColor(Color.parseColor("#E74C3C"));
            tvLeftOf.setText(String.format("Excedeu o orçamento de €%.2f", budget));
        } else {
            tvBudgetRemaining.setTextColor(Color.parseColor("#3FA4CE"));
            tvLeftOf.setText(String.format("Restante de €%.2f", budget));
        }

        // === Progresso ===
        double percent = (budget > 0) ? (total / budget) * 100.0 : 0.0;
        int clampedProgress = (int) Math.min(percent, 100);

        tvProgressDetail.setText(String.format(Locale.getDefault(), "%.0f%%", percent));

        int corProgresso = percent > 100 ? Color.parseColor("#E74C3C") : Color.parseColor("#3FA4CE");
        int corFundo = Color.parseColor("#E0E0E0");

        progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(corProgresso));
        progressBar.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(corFundo));

        ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 0, clampedProgress);
        animation.setDuration(800);
        animation.start();

        // === Gráfico de pizza ===
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> cores = new ArrayList<>();

        for (Map.Entry<String, Double> e : gastosPorCategoria.entrySet()) {
            entries.add(new PieEntry(e.getValue().floatValue(), e.getKey()));

            switch (e.getKey().toLowerCase()) {
                case "alimentacao": cores.add(Color.parseColor("#FFB74D")); break;
                case "transporte": cores.add(Color.parseColor("#4FC3F7")); break;
                case "lazer": cores.add(Color.parseColor("#BA68C8")); break;
                case "saude": cores.add(Color.parseColor("#81C784")); break;
                case "casa": cores.add(Color.parseColor("#A1887F")); break;
                case "educacao": cores.add(Color.parseColor("#64B5F6")); break;
                case "supermercado": cores.add(Color.parseColor("#FFD54F")); break;
                case "subscricao": cores.add(Color.parseColor("#9575CD")); break;
                case "outro": cores.add(Color.parseColor("#B0BEC5")); break;
                default: cores.add(Color.parseColor("#AED581")); break;
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(cores);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.DKGRAY);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate();
    }


    // 🔁 Garante atualização quando voltas ao fragmento
    @Override
    public void onResume() {
        super.onResume();
        atualizarDespesasRecentes();
    }

    @Override
    public void onDespesaAlterada() {
        refreshAll();
    }
}
