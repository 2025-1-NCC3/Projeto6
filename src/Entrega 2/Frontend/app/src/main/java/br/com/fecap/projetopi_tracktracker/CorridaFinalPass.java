package br.com.fecap.projetopi_tracktracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class CorridaFinalPass extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corrida_final_pass);
    }

    public void voltarHome(View view) {
        Intent intent = new Intent(this, HomePassageiro.class);
        startActivity(intent);
        finish();
    }
}
