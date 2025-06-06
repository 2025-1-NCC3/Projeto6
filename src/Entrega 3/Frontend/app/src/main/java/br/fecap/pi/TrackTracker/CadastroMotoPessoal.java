package br.fecap.pi.TrackTracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import br.fecap.pi.TrackTracker.R;

public class CadastroMotoPessoal extends AppCompatActivity {

    private EditText etNome, etSobrenome, etCPF, etDataNascimento, etEmail, etTelefone, etEndereco;

    private String nome, sobrenome, cpf, dataNascimento, email, telefone, endereco;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Vinculando os campos
        setContentView(R.layout.activity_cadastro_moto_pessoal);
        etNome = findViewById(R.id.etNome);
        etSobrenome = findViewById(R.id.etSobrenome);
        etCPF = findViewById(R.id.etCPF);
        etDataNascimento = findViewById(R.id.etDataNascimento);
        etEmail = findViewById(R.id.etEmail);
        etTelefone = findViewById(R.id.etTelefone);
        etEndereco = findViewById(R.id.etEndereco);

    }

    public void seguinteCarro(View view){

        // Captura dos dados digitados
        nome = etNome.getText().toString().trim();
        sobrenome = etSobrenome.getText().toString().trim();
        cpf = etCPF.getText().toString().trim();
        dataNascimento = etDataNascimento.getText().toString().trim();
        email = etEmail.getText().toString().trim();
        telefone = etTelefone.getText().toString().trim();
        endereco = etEndereco.getText().toString().trim();

        // Envio dos dados para próxima tela
        Intent intent = new Intent(this, CadastroMotoCar.class);
        intent.putExtra("nome", nome);
        intent.putExtra("sobrenome", sobrenome);
        intent.putExtra("cpf", cpf);
        intent.putExtra("dataNascimento", dataNascimento);
        intent.putExtra("email", email);
        intent.putExtra("telefone", telefone);
        intent.putExtra("endereco", endereco);
        startActivity(intent);
    }

    public void voltar(View view){
        finish();
    }

}
