package br.com.fecap.projetopi_tracktracker;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class CorridaPass extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corrida_pass);

        Intent intent = new Intent(CorridaPass.this, CorridaFinalPass.class);
        startActivity(intent);
        finish();

    }

}
