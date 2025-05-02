package br.fecap.pi.TrackTracker;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import br.fecap.pi.TrackTracker.R;

public class NovidadesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novidades);
    }

    public void finish(View view) {
        finish();
    }
}
