package br.fecap.pi.TrackTracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;

import br.fecap.pi.TrackTracker.R;

public class CorridaFinalPass extends AppCompatActivity {

    private RatingBar ratingBarConf, ratingBarSeg, ratingBarVeic, ratingBarConv;
    private Button btnEnviarAval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corrida_final_pass);

        // Inicializando os RatingBars
        ratingBarConf = findViewById(R.id.ratingBarConf);
        ratingBarSeg = findViewById(R.id.ratingBarSeg);
        ratingBarVeic = findViewById(R.id.ratingBarVeic);
        ratingBarConv = findViewById(R.id.ratingBarConv);

        btnEnviarAval = findViewById(R.id.btnEnviarAval);

        btnEnviarAval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Pegando os valores de avaliação
                float conforto = ratingBarConf.getRating();
                float seguranca = ratingBarSeg.getRating();
                float veiculo = ratingBarVeic.getRating();
                float conversa = ratingBarConv.getRating();

                // Aplicando os pesos (convertendo para porcentagem)
                float avaliacaoFinal = (
                        (conforto * 0.25f) +
                                (seguranca * 0.35f) +
                                (veiculo * 0.20f) +
                                (conversa * 0.20f)
                );

                DecimalFormat df = new DecimalFormat("0.0");
                String notaFormatadaStr = df.format(avaliacaoFinal);
                float notaFormatadaF = Float.parseFloat(notaFormatadaStr);

                // Exemplo: salvando como float para Intent
                Intent intent = new Intent(CorridaFinalPass.this, HomePassageiro.class);
                intent.putExtra("avaliacaoFinal", notaFormatadaF);
                startActivity(intent);
            }
        });
    }
}
