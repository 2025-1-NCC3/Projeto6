package br.fecap.pi.TrackTracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import br.fecap.pi.TrackTracker.R;

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
