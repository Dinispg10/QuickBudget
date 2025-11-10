package com.example.quickbudget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Arrays;

public class DetalheDespesaDialogFragment extends DialogFragment {

    // Interface para atualizar a lista após editar
    public interface OnDespesaAlteradaListener { void onDespesaAlterada(); }

    private OnDespesaAlteradaListener listener; // listener do callback
    private static final String ARG_ID = "arg_id"; // argumento com o ID da despesa
    private Despesa despesa; // despesa atual

    // Cria nova instância do diálogo com o ID da despesa
    public static DetalheDespesaDialogFragment nova(int idDespesa) {
        DetalheDespesaDialogFragment f = new DetalheDespesaDialogFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_ID, idDespesa);
        f.setArguments(b);
        return f;
    }

    // Define o listener externo
    public void setOnDespesaAlteradaListener(OnDespesaAlteradaListener l) {
        this.listener = l;
    }

    // Cria o diálogo
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        android.view.View view = inflater.inflate(R.layout.fragment_despesa_detalhe_dialog, null);

        // Campos do layout
        EditText editDescricao = view.findViewById(R.id.editTextEditDescricao);
        EditText editValor = view.findViewById(R.id.editTextEditValor);
        Spinner spinnerCategoria = view.findViewById(R.id.spinnerEditCategoria);
        Spinner spinnerRecorrencia = view.findViewById(R.id.spinnerEditRecorrencia);

        // Configura os spinners
        String[] categorias = getResources().getStringArray(R.array.categorias_array);
        String[] recs = getResources().getStringArray(R.array.tipos_recorrencia);

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, Arrays.asList(categorias));
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(catAdapter);

        ArrayAdapter<String> recAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, Arrays.asList(recs));
        recAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRecorrencia.setAdapter(recAdapter);

        // Busca a despesa pelo ID
        int id = getArguments() != null ? getArguments().getInt(ARG_ID, -1) : -1;
        if (id != -1) {
            DespesaDAO dao = new DespesaDAO(requireContext());
            despesa = dao.obterPorId(id);
            dao.fechar();
        }

        // Preenche os campos com os dados da despesa
        if (despesa != null) {
            editDescricao.setText(despesa.getDescricao());
            editValor.setText(String.valueOf(despesa.getValor()));

            int catIndex = Arrays.asList(categorias).indexOf(despesa.getCategoria());
            if (catIndex < 0) catIndex = 0;
            spinnerCategoria.setSelection(catIndex);

            int recIndex = Arrays.asList(recs).indexOf(despesa.getRecorrencia());
            if (recIndex < 0) recIndex = 0;
            spinnerRecorrencia.setSelection(recIndex);
        }

        // Cria o diálogo com os botões
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Detalhe da despesa")
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .setNeutralButton("Eliminar", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button btnGuardar = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button btnEliminar = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);

            // 🟢 Guardar alterações
            btnGuardar.setOnClickListener(v -> {
                if (despesa == null) {
                    editDescricao.setError("Despesa não encontrada");
                    return;
                }

                String desc = editDescricao.getText().toString().trim();
                String valorStr = editValor.getText().toString().trim();

                if (TextUtils.isEmpty(desc)) {
                    editDescricao.setError("Descrição obrigatória");
                    return;
                }
                if (TextUtils.isEmpty(valorStr)) {
                    editValor.setError("Valor obrigatório");
                    return;
                }

                double valor;
                try {
                    valor = Double.parseDouble(valorStr.replace(",", "."));
                    if (valor < 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    editValor.setError("Valor inválido");
                    return;
                }

                String categoria = spinnerCategoria.getSelectedItem().toString();
                String recorrencia = spinnerRecorrencia.getSelectedItem().toString();

                despesa.setDescricao(desc);
                despesa.setValor(valor);
                despesa.setCategoria(categoria);
                despesa.setRecorrencia(recorrencia);
                despesa.setTimestamp(System.currentTimeMillis());

                // Atualizar na BD
                DespesaDAO dao = new DespesaDAO(requireContext());
                dao.atualizar(despesa);
                dao.fechar();

                // ✅ Mostra mensagem de sucesso
                Toast.makeText(requireContext(), "Despesa atualizada!", Toast.LENGTH_SHORT).show();

                if (listener != null) listener.onDespesaAlterada();
                dialog.dismiss();
            });

            // 🔴 Eliminar despesa (com confirmação)
            btnEliminar.setOnClickListener(v -> {
                if (despesa != null) {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Eliminar despesa")
                            .setMessage("Tens a certeza que queres eliminar esta despesa?")
                            .setNegativeButton("Cancelar", null)
                            .setPositiveButton("Sim", (confirmDialog, which) -> {
                                DespesaDAO dao = new DespesaDAO(requireContext());
                                dao.eliminar(despesa.getId());
                                dao.fechar();

                                // ✅ Mostra mensagem de sucesso
                                Toast.makeText(requireContext(), "Despesa eliminada!", Toast.LENGTH_SHORT).show();

                                if (listener != null) listener.onDespesaAlterada();
                                dialog.dismiss();
                            })
                            .show();
                }
            });
        });


        return dialog;
    }
}
