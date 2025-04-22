package br.com.fecap.projetopi_tracktracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CadastroMotoSelfie extends AppCompatActivity {


    public void voltar(View view){
        finish();
    }

    public void finalizarCadastro(View view){
        Intent telaLogin = new Intent(this, LoginMoto.class);
        startActivity(telaLogin);
        finish();
    }

    @Override
    protected void onStart(){ super.onStart(); }

    @Override
    protected void onRestart(){ super.onRestart(); }

    @Override
    protected void onResume(){ super.onResume(); }

    @Override
    protected void onPause(){ super.onPause(); }

    @Override
    protected void onStop(){ super.onStop(); }

    @Override
    protected void onDestroy(){ super.onDestroy(); }
}
