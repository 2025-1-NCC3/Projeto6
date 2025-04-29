package br.fecap.pi.TrackTracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import br.fecap.pi.TrackTracker.R;

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
