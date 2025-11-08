package com.example.quickbudget;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Fragment dashboardFragment;
    private Fragment addDespesaFragment;
    private Fragment historicoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // === ✅ Garantir que o orçamento da semana está criado ===
        long inicioSemana = DateUtils.getWeekStartMillis();
        BudgetDAO bdao = new BudgetDAO(this);
        bdao.getOrCreateBudgetAtual(inicioSemana);
        bdao.fechar();

        // === ✅ Verificar despesas recorrentes (semanal/mensal) ===
        verificarDespesasRecorrentes();

        // === ⚙️ Configurar o menu inferior ===
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        dashboardFragment = new DashboardFragment();
        addDespesaFragment = new AddDespesaFragment();
        historicoFragment = new HistoricoFragment();

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();
            if (id == R.id.menu_dashboard) {
                selected = dashboardFragment;
            } else if (id == R.id.menu_add) {
                selected = addDespesaFragment;
            } else if (id == R.id.menu_history) {
                selected = historicoFragment;
            }

            if (selected != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selected)
                        .commit();
                return true;
            }
            return false;
        });

        // Abrir por defeito o Dashboard
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.menu_dashboard);
        }
    }

    /**
     * 🌀 Cria automaticamente despesas recorrentes (Semanal / Mensal)
     * caso ainda não existam para o período atual.
     */
    private void verificarDespesasRecorrentes() {
        long inicioSemana = DateUtils.getWeekStartMillis();
        DespesaDAO dao = new DespesaDAO(this);
        dao.gerarDespesasRecorrentes(inicioSemana);
        dao.fechar();
    }

}
