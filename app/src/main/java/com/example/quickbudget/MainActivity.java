package com.example.quickbudget;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private Fragment dashboardFragment;
    private Fragment addDespesaFragment;
    private Fragment historicoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BudgetStorage.ensureCurrentWeekBudgetStored(this);
        DespesaStorage.verificarDespesasRecorrentes(this);

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

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.menu_dashboard);
        }
    }
}
