package br.com.fecap.projetopi_tracktracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CadastroPassSenha extends AppCompatActivity {

    private EditText etSenha;
    private String senha;

    //infos da tela anterior
    private String nome, sobrenome, cpf, dataNascimento, email, telefone, endereco;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro_pass_senha);

        etSenha = findViewById(R.id.etSenha);

        // Recuperando os dados da ultima tela
        nome = getIntent().getStringExtra("nome");
        sobrenome = getIntent().getStringExtra("sobrenome");
        cpf = getIntent().getStringExtra("cpf");
        dataNascimento = getIntent().getStringExtra("dataNascimento");
        email = getIntent().getStringExtra("email");
        telefone = getIntent().getStringExtra("telefone");
        endereco = getIntent().getStringExtra("endereco");

    }




    public void voltar(View view){
        finish();
    }
}