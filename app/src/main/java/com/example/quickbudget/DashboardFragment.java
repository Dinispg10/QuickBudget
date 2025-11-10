package com.example.quickbudget;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
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

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardFragment extends Fragment implements DetalheDespesaDialogFragment.OnDespesaAlteradaListener {

    // Variáveis da interface
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

        // Liga elementos do layout
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

        // Mostra intervalo da semana atual
        tvWeekSummary.setText("Resumo da semana (" + DateUtils.getCurrentWeekRangeString() + ")");

        // Botão de sair
        ImageButton buttonExit = view.findViewById(R.id.buttonExit);
        buttonExit.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle("Sair")
                .setMessage("Tens a certeza que queres sair da aplicação?")
                .setNegativeButton("Não", null)
                .setPositiveButton("Sim", (dialog, which) -> requireActivity().finishAffinity())
                .show());

        // Inicializa componentes
        setupRecyclerView();
        setupPieChart();
        setupBudgetUpdate();

        // Atualiza dados
        refreshAll();

        return view;
    }

    // Atualiza lista de despesas recentes
    private void atualizarDespesasRecentes() {
        DespesaDAO dao = new DespesaDAO(requireContext());
        long inicioSemana = DateUtils.getWeekStartMillis();
        long fimSemana = DateUtils.getWeekEndMillis();

        List<Despesa> todas = dao.listarTodas();
        dao.fechar();

        // Filtra despesas da semana atual
        List<Despesa> semana = new ArrayList<>();
        for (Despesa d : todas) {
            if (d.getTimestamp() >= inicioSemana && d.getTimestamp() <= fimSemana) {
                semana.add(d);
            }
        }

        // Ordena por data
        semana.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

        // Mostra no máximo 2
        List<Despesa> recentes = semana.size() > 2 ? semana.subList(0, 2) : semana;

        // Configura adapter
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

    // Configuração do RecyclerView
    private void setupRecyclerView() {
        rvRecentExpenses.setHasFixedSize(true);
        rvRecentExpenses.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    // Atualiza orçamento semanal
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

    // Configuração inicial do gráfico
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

    // Atualiza todos os dados da semana
    public void refreshAll() {
        // Carrega despesas
        DespesaDAO ddao = new DespesaDAO(requireContext());
        List<Despesa> despesas = ddao.listarTodas();
        ddao.fechar();

        double total = 0.0;
        Map<String, Double> gastosPorCategoria = new HashMap<>();

        // Intervalo da semana atual
        long inicioSemana = DateUtils.getWeekStartMillis();
        long fimSemana = DateUtils.getWeekEndMillis();

        // Filtra e soma
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

        // Atualiza lista
        atualizarDespesasRecentes();

        // Lê orçamento
        BudgetDAO bdao = new BudgetDAO(requireContext());
        double budget = bdao.getBudgetPorSemana(inicioSemana);
        bdao.fechar();

        double restante = budget - total;
        double mediaPorDia = total / 7.0;

        // Atualiza textos
        tvAvgDay.setText(String.format(Locale.getDefault(), "€%.2f", mediaPorDia));
        tvWeeklyBudget.setText(String.format("Orçamento semanal atual: €%.2f", budget));
        tvTotalSpent.setText(String.format("€%.2f", total));
        tvBudgetRemaining.setText(String.format("€%.2f", restante));

        // Cores do orçamento
        if (restante < 0) {
            tvBudgetRemaining.setTextColor(Color.parseColor("#E74C3C"));
            tvLeftOf.setText(String.format("Excedeu o orçamento de €%.2f", budget));
        } else {
            tvBudgetRemaining.setTextColor(Color.parseColor("#3FA4CE"));
            tvLeftOf.setText(String.format("Restante de €%.2f", budget));
        }

        // Barra de progresso
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

        // Gráfico de pizza
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> cores = new ArrayList<>();

        // Cria fatias
        for (Map.Entry<String, Double> e : gastosPorCategoria.entrySet()) {
            entries.add(new PieEntry(e.getValue().floatValue(), e.getKey()));

            String chave = Normalizer.normalize(e.getKey().toLowerCase(), Normalizer.Form.NFD)
                    .replaceAll("[^\\p{ASCII}]", "");

            switch (chave) {
                case "alimentacao": cores.add(Color.parseColor("#E53935")); break;
                case "transporte": cores.add(Color.parseColor("#1E88E5")); break;
                case "lazer": cores.add(Color.parseColor("#8E24AA")); break;
                case "saude": cores.add(Color.parseColor("#43A047")); break;
                case "casa": cores.add(Color.parseColor("#6D4C41")); break;
                case "educacao": cores.add(Color.parseColor("#FFA726")); break;
                case "supermercado": cores.add(Color.parseColor("#FDD835")); break;
                case "subscricao": cores.add(Color.parseColor("#00ACC1")); break;
                case "outro": cores.add(Color.parseColor("#9E9E9E")); break;
                default: cores.add(Color.parseColor("#00897B")); break;
            }
        }

        // Cria dataset
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(cores);
        dataSet.setValueTextSize(13f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTypeface(Typeface.DEFAULT_BOLD);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        // Aparência do gráfico
        pieChart.setDrawEntryLabels(false);
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(55f);
        pieChart.setHoleRadius(45f);

        // Legenda
        Legend legenda = pieChart.getLegend();
        legenda.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legenda.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legenda.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legenda.setDrawInside(false);
        legenda.setWordWrapEnabled(true);
        legenda.setTextSize(12f);
        legenda.setXEntrySpace(10f);
        legenda.setYEntrySpace(5f);

        // Margem inferior
        pieChart.setExtraOffsets(0, 0, 0, 10);

        // Animação
        pieChart.animateY(1000, Easing.EaseInOutQuad);
        pieChart.invalidate();
    }

    // Atualiza quando volta ao fragmento
    @Override
    public void onResume() {
        super.onResume();
        atualizarDespesasRecentes();
    }

    // Listener de alteração
    @Override
    public void onDespesaAlterada() {
        refreshAll();
    }
}
