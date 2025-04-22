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

public class CadastroMotoCar extends AppCompatActivity {
      
// 1. Declarar os campos EditText
EditText etCNH, etMarca, etModelo, etAno, etPlaca, etCategoria;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_cadastro_moto_car);

    // 2. Associar os EditText aos componentes do layout
    etCNH = findViewById(R.id.etCNH);
    etMarca = findViewById(R.id.etMarca);
    etModelo = findViewById(R.id.etModelo);
    etAno = findViewById(R.id.etAno);
    etPlaca = findViewById(R.id.etPlaca);
    etCategoria = findViewById(R.id.etCategoria);
}

public void seguinteSenha(View view){
    // 3. Capturar os dados digitados
    String cnh = etCNH.getText().toString();
    String marca = etMarca.getText().toString();
    String modelo = etModelo.getText().toString();
    String ano = etAno.getText().toString();
    String placa = etPlaca.getText().toString();
    String categoria = etCategoria.getText().toString();

    // Aqui você pode mandar para outra tela com putExtra ou salvar em banco depois
    Intent telaSenha = new Intent(this, CadastroMotoSenha.class);

    // Exemplo de envio via Intent (opcional, caso queira usar depois)
    telaSenha.putExtra("CNH", cnh);
    telaSenha.putExtra("MARCA", marca);
    telaSenha.putExtra("MODELO", modelo);
    telaSenha.putExtra("ANO", ano);
    telaSenha.putExtra("PLACA", placa);
    telaSenha.putExtra("CATEGORIA", categoria);

    startActivity(telaSenha);
}

public void voltar(View view){
    finish();
}

    @Override
    protected void onStart(){
            super.onStart();
        }

        @Override
        protected void onRestart(){
            super.onRestart();
        }

        @Override
        protected void onResume(){
            super.onResume();
        }

        @Override
        protected void onPause(){
            super.onPause();
        }

        @Override
        protected void onStop(){
            super.onStop();
        }

        @Override
        protected void onDestroy(){
            super.onDestroy();
        }
    }

