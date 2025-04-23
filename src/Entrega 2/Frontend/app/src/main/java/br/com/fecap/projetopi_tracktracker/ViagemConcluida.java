package br.com.fecap.projetopi_tracktracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ViagemConcluida extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viagem_concluida);
    }

    public void voltarHome(View view) {
        Intent intent = new Intent(this, HomeMoto.class);
        startActivity(intent);
        finish();
    }
}
