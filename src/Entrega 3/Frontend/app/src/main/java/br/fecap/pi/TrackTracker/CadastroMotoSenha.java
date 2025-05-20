package br.fecap.pi.TrackTracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import br.fecap.pi.TrackTracker.R;

public class CadastroMotoSenha extends AppCompatActivity {

    private EditText etSenha, etConfirmaSenha;
    private String senha;

    // Dados anteriores
    private String nome, sobrenome, cpf, dataNascimento, email, telefone, endereco;
    private String cnh, marca, modelo, ano, placa, categoria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro_moto_senha);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etSenha = findViewById(R.id.etSenha);
        etConfirmaSenha = findViewById(R.id.etConfirmaSenha);

        // Pegando os dados da Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            nome = extras.getString("nome");
            sobrenome = extras.getString("sobrenome");
            cpf = extras.getString("cpf");
            dataNascimento = extras.getString("dataNascimento");
            email = extras.getString("email");
            telefone = extras.getString("telefone");
            endereco = extras.getString("endereco");

            cnh = extras.getString("cnh");
            marca = extras.getString("marca");
            modelo = extras.getString("modelo");
            ano = extras.getString("ano");
            placa = extras.getString("placa");
            categoria = extras.getString("categoria");
        }
    }

    public void seguinteFoto(View view) {
        String senhaDigitada = etSenha.getText().toString();
        String confirmaSenha = etConfirmaSenha.getText().toString();

        if (!senhaDigitada.equals(confirmaSenha)) {
            Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, CadastroMotoFoto.class);

        // Enviando os dados pra próxima tela
        intent.putExtra("nome", nome);
        intent.putExtra("sobrenome", sobrenome);
        intent.putExtra("cpf", cpf);
        intent.putExtra("dataNascimento", dataNascimento);
        intent.putExtra("email", email);
        intent.putExtra("telefone", telefone);
        intent.putExtra("endereco", endereco);
        intent.putExtra("cnh", cnh);
        intent.putExtra("marca", marca);
        intent.putExtra("modelo", modelo);
        intent.putExtra("ano", ano);
        intent.putExtra("placa", placa);
        intent.putExtra("categoria", categoria);
        intent.putExtra("senha", senhaDigitada);

        startActivity(intent);
    }

    public void voltar(View view) {
        finish();
    }
}
