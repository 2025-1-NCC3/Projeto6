package br.fecap.pi.TrackTracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import br.fecap.pi.TrackTracker.R;

public class CadastroPassSenha extends AppCompatActivity {

    private EditText etSenha, etConfirmaSenha;
    private String senha;

    //infos da tela anterior
    private String nome, sobrenome, cpf, dataNascimento, email, telefone, endereco;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro_pass_senha);

        etSenha = findViewById(R.id.etSenha);
        etConfirmaSenha = findViewById(R.id.etConfirmaSenha);

        // Recuperando os dados da ultima tela
        nome = getIntent().getStringExtra("nome");
        sobrenome = getIntent().getStringExtra("sobrenome");
        cpf = getIntent().getStringExtra("cpf");
        dataNascimento = getIntent().getStringExtra("dataNascimento");
        email = getIntent().getStringExtra("email");
        telefone = getIntent().getStringExtra("telefone");
        endereco = getIntent().getStringExtra("endereco");

    }

    public void seguinteFoto(View view){

        String senhaDigitada = etSenha.getText().toString();
        String confirmaSenha = etConfirmaSenha.getText().toString();
        senha = etSenha.getText().toString().trim();

        if (!senhaDigitada.equals(confirmaSenha)) {
            Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show();
            return;
        }


        Intent intent = new Intent(this, CadastroPassFoto.class);
        intent.putExtra("senha", senha);
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

