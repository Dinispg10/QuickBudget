package com.example.quickbudget;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Button;

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
import java.util.Map;

public class DashboardFragment extends Fragment implements DetalheDespesaDialogFragment.OnDespesaAlteradaListener {

    private RecyclerView rvRecentExpenses;
    private DespesaAdapter adapter;
    private PieChart pieChart;
    private TextView tvWeekSummary, tvProgress, tvTotalSpent, tvWeeklyBudget, tvBudgetRemaining, tvLeftOf, tvAvgDay;
    private ProgressBar progressBar;
    private EditText editNewBudget;
    private Button buttonUpdateBudget;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        rvRecentExpenses = view.findViewById(R.id.recyclerViewRecentExpenses);
        pieChart = view.findViewById(R.id.pieChartCategories);
        tvWeekSummary = view.findViewById(R.id.textViewWeekSummary);
        tvTotalSpent = view.findViewById(R.id.textViewTotalSpent);
        tvWeeklyBudget = view.findViewById(R.id.textViewWeeklyBudget);
        tvProgress = view.findViewById(R.id.textViewProgress);
        progressBar = view.findViewById(R.id.progressBarBudget);
        editNewBudget = view.findViewById(R.id.editTextNewBudget);
        buttonUpdateBudget = view.findViewById(R.id.buttonUpdateBudget);
        tvBudgetRemaining = view.findViewById(R.id.textViewBudgetRemaining);
        tvLeftOf = view.findViewById(R.id.textViewLeftOf);
        tvAvgDay = view.findViewById(R.id.textViewAvgDay);

        tvWeekSummary.setText("Resumo da semana (" + DateUtils.getCurrentWeekRangeString() + ")");
        setupRecyclerView();
        setupPieChart();
        setupBudgetUpdate();

        refreshAll();

        return view;
    }

    private void setupRecyclerView() {
        rvRecentExpenses.setHasFixedSize(true);
        rvRecentExpenses.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DespesaAdapter(DespesaStorage.getDespesasRecentes(requireContext(), 2),
                despesa -> {
                    DetalheDespesaDialogFragment dialog = DetalheDespesaDialogFragment.nova(despesa.getId());
                    dialog.setOnDespesaAlteradaListener(this);
                    dialog.show(getParentFragmentManager(), "Detalhe");
                });
        rvRecentExpenses.setAdapter(adapter);
    }

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

    private void setupBudgetUpdate() {
        buttonUpdateBudget.setOnClickListener(v -> {
            String valueStr = editNewBudget.getText().toString().trim();
            if (TextUtils.isEmpty(valueStr)) {
                editNewBudget.setError("Insere um novo orçamento!");
                return;
            }
            try {
                double valor = Double.parseDouble(valueStr.replace(",", "."));
                BudgetStorage.setWeeklyBudget(requireContext(), valor);
                BudgetStorage.ensureCurrentWeekBudgetStored(requireContext());
                refreshAll();
                editNewBudget.setText("");
            } catch (NumberFormatException ignored) {
                editNewBudget.setError("Valor de orçamento inválido!");
            }
        });
    }

    public void refreshAll() {
        List<Despesa> semana = DespesaStorage.getDespesasSemana(requireContext());
        double total = 0.0;
        Map<String, Double> gastosPorCategoria = new HashMap<>();
        for (Despesa d : semana) {
            total += d.getValor();
            double sum = gastosPorCategoria.getOrDefault(d.getCategoria(), 0.0);
            gastosPorCategoria.put(d.getCategoria(), sum + d.getValor());
        }

        adapter.setItems(DespesaStorage.getDespesasRecentes(requireContext(), 2));
        adapter.notifyDataSetChanged();

        double budget = BudgetStorage.getWeeklyBudget(requireContext());
        double restante = budget - total;

        // Calcular média por dia da semana atual
        int diasSemana = 7; // ou podes usar DateUtils.getDiasDaSemanaAtual() se tiveres algo assim
        double mediaPorDia = total / diasSemana;

// Atualizar texto da média
        tvAvgDay.setText(String.format("€%.2f", mediaPorDia));

        // Mostrar valores principais
        tvWeeklyBudget.setText(String.format("€%.2f", budget));
        tvTotalSpent.setText(String.format("€%.2f", total));
        tvBudgetRemaining.setText(String.format("€%.2f", restante));

        // Mudar cor e texto conforme positivo/negativo
        if (restante < 0) {
            tvBudgetRemaining.setTextColor(Color.parseColor("#E74C3C")); // vermelho suave
            tvLeftOf.setText(String.format("acima do orçamento (€%.2f)", budget));
        } else {
            tvBudgetRemaining.setTextColor(Color.parseColor("#3FA4CE")); // azul pastel suave
            tvLeftOf.setText(String.format("restante de €%.2f", budget));
        }

        // Atualizar progresso (sem ultrapassar 100)
        int progress = budget == 0.0 ? 0 : (int) ((total / budget) * 100);
        progress = Math.min(progress, 100);
        progressBar.setProgress(progress);
        tvProgress.setText(String.format("%d%%", progress));

        // ==============================
        // Gráfico de pizza com cores por categoria
        // ==============================
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> cores = new ArrayList<>();

        for (Map.Entry<String, Double> e : gastosPorCategoria.entrySet()) {
            entries.add(new PieEntry(e.getValue().floatValue(), e.getKey()));

            // Define uma cor específica por categoria
            switch (e.getKey().toLowerCase()) {
                case "alimentação":
                case "alimentacao":
                    cores.add(Color.parseColor("#FFB74D")); // 🍊 Laranja suave
                    break;

                case "transporte":
                    cores.add(Color.parseColor("#4FC3F7")); // 🚗 Azul claro
                    break;

                case "lazer":
                    cores.add(Color.parseColor("#BA68C8")); // 🎮 Roxo pastel
                    break;

                case "saúde":
                case "saude":
                    cores.add(Color.parseColor("#81C784")); // 💊 Verde médio
                    break;

                case "casa":
                    cores.add(Color.parseColor("#A1887F")); // 🏠 Castanho suave
                    break;

                case "educação":
                case "educacao":
                    cores.add(Color.parseColor("#64B5F6")); // 📚 Azul pastel
                    break;

                case "supermercado":
                    cores.add(Color.parseColor("#FFD54F")); // 🛒 Amarelo suave
                    break;

                case "subscrição":
                case "subscricao":
                    cores.add(Color.parseColor("#9575CD")); // 📺 Roxo acinzentado
                    break;

                case "outro":
                case "outros":
                    cores.add(Color.parseColor("#B0BEC5")); // ⚙️ Cinzento claro
                    break;

                default:
                    cores.add(Color.parseColor("#AED581")); // 🌿 Verde claro (default)
                    break;
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


    @Override
    public void onDespesaAlterada() {
        refreshAll();
    }
}
