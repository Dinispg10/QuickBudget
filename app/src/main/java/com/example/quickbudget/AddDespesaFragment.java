package com.example.quickbudget;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AddDespesaFragment extends Fragment {

    // Campos do formulário
    private EditText editDescricao, editValor;
    private Spinner spinnerCategoria, spinnerRecorrencia;
    private Button buttonGuardar;

    public AddDespesaFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_despesa, container, false);

        // Liga elementos do layout
        editDescricao = view.findViewById(R.id.editTextDescription);
        editValor = view.findViewById(R.id.editTextAmount);
        spinnerCategoria = view.findViewById(R.id.spinnerCategory);
        spinnerRecorrencia = view.findViewById(R.id.spinner_recorrencia);
        buttonGuardar = view.findViewById(R.id.buttonSave);

        // Configura spinners e botão
        setupSpinners();
        setupButton();

        return view;
    }

    // Configura as listas de categoria e recorrência
    private void setupSpinners() {
        ArrayAdapter adapterCategorias = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.categorias_array,
                android.R.layout.simple_spinner_item
        );
        adapterCategorias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapterCategorias);
        spinnerCategoria.setSelection(0, false);

        ArrayAdapter adapterRecorrencia = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.tipos_recorrencia,
                android.R.layout.simple_spinner_item
        );
        adapterRecorrencia.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRecorrencia.setAdapter(adapterRecorrencia);
        spinnerRecorrencia.setSelection(0, false);
    }

    // Configura o botão de guardar
    private void setupButton() {
        buttonGuardar.setOnClickListener(v -> {
            // Lê campos de texto
            String descricao = editDescricao.getText().toString().trim();
            String valorStr = editValor.getText().toString().trim();

            // Valida campos
            if (TextUtils.isEmpty(descricao)) {
                editDescricao.setError("Insere uma descrição");
                return;
            }
            if (TextUtils.isEmpty(valorStr)) {
                editValor.setError("Insere um valor");
                return;
            }

            // Converte valor para número
            double valor;
            try {
                valor = Double.parseDouble(valorStr.replace(",", "."));
            } catch (NumberFormatException e) {
                editValor.setError("Valor inválido");
                return;
            }

            // Obtém categoria e recorrência
            String categoria = spinnerCategoria.getSelectedItem().toString();
            String recorrencia = spinnerRecorrencia.getSelectedItem().toString();

            // Verifica seleções válidas
            if (categoria.equals("Selecione a categoria")) {
                Toast.makeText(requireContext(), "Escolhe uma categoria!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (recorrencia.equals("Selecione a recorrência")) {
                Toast.makeText(requireContext(), "Escolhe uma recorrência!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Cria nova despesa
            Despesa nova = new Despesa(
                    descricao,
                    categoria,
                    valor,
                    System.currentTimeMillis(),
                    recorrencia
            );

            // Insere na base de dados
            DespesaDAO dao = new DespesaDAO(requireContext());
            long idInserido = dao.inserir(nova);
            dao.fechar();

            // Mostra resultado
            if (idInserido != -1) {
                Toast.makeText(requireContext(), "Despesa guardada com sucesso!", Toast.LENGTH_SHORT).show();
                editDescricao.setText("");
                editValor.setText("");
                spinnerCategoria.setSelection(0);
                spinnerRecorrencia.setSelection(0);
            } else {
                Toast.makeText(requireContext(), "Erro ao guardar despesa!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
