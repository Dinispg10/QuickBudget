package com.example.quickbudget;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * MainActivity
 * ------------
 * Atividade principal da aplicação.
 * Responsável por inicializar a base de dados, verificar despesas recorrentes
 * e gerir a navegação entre os fragmentos (Dashboard, Adicionar e Histórico)
 * através do menu inferior.
 */
public class MainActivity extends AppCompatActivity {

    private Fragment dashboardFragment;
    private Fragment addDespesaFragment;
    private Fragment historicoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Garante que existe um orçamento para a semana atual
        long inicioSemana = DateUtils.getWeekStartMillis();
        BudgetDAO bdao = new BudgetDAO(this);
        bdao.getOrCreateBudgetAtual(inicioSemana);
        bdao.fechar();

        // Gera automaticamente despesas recorrentes (semanal/mensal)
        verificarDespesasRecorrentes();

        // Configura o menu inferior (BottomNavigationView)
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Inicializa fragmentos principais
        dashboardFragment = new DashboardFragment();
        addDespesaFragment = new AddDespesaFragment();
        historicoFragment = new HistoricoFragment();

        // Define comportamento do menu inferior
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

        // Abre o Dashboard por defeito
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.menu_dashboard);
        }
    }

    /**
     * Cria automaticamente as despesas recorrentes (semanais/mensais)
     * caso ainda não existam para o período atual.
     */
    private void verificarDespesasRecorrentes() {
        long inicioSemana = DateUtils.getWeekStartMillis();
        DespesaDAO dao = new DespesaDAO(this);
        dao.gerarDespesasRecorrentes(inicioSemana);
        dao.fechar();
    }
}
