package br.fecap.pi.TrackTracker;

import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.*;

public class CorridaPassEmAndamento extends AppCompatActivity {

    private MapView mapView;
    private IMapController mapController;
    private GeoPoint destino;
    private Marker marcadorUsuario;

    private TextView txtDistancia, txtTempo;
    private Button btnCentralizar, btnConcluir, btnDesvio, btnChegarFinal;
    private ImageView imgMicrofone;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private static final int LOCATION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_corrida_pass_em_andamento);

        Configuration.getInstance().setUserAgentValue(getPackageName());

        mapView = findViewById(R.id.mapView);
        mapView.setMultiTouchControls(true);
        mapController = mapView.getController();
        mapController.setZoom(18.0);

        // Inicia views
        txtDistancia = findViewById(R.id.txtDistância);
        txtTempo = findViewById(R.id.txtTempo);
        btnCentralizar = findViewById(R.id.btnCentralizar);
        btnConcluir = findViewById(R.id.btnConcluirCorr);
        btnDesvio = findViewById(R.id.btnAtivaDetec);
        btnChegarFinal = findViewById(R.id.btnChegarNoFinal);
        imgMicrofone = findViewById(R.id.imgMicroIc);

        // Localização
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = location -> {
            GeoPoint posAtual = new GeoPoint(location.getLatitude(), location.getLongitude());

            if (marcadorUsuario == null) {
                marcadorUsuario = new Marker(mapView);
                marcadorUsuario.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marcadorUsuario.setTitle("Você");
                mapView.getOverlays().add(marcadorUsuario);
            }

            marcadorUsuario.setPosition(posAtual);
            mapView.invalidate();
            atualizarTempoEDistancia(posAtual);
        };

        // Pedir permissão e iniciar localização
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            iniciarLocalizacao();
        }

        // Centralizar no usuário
        btnCentralizar.setOnClickListener(v -> {
            if (marcadorUsuario != null) {
                mapController.setCenter(marcadorUsuario.getPosition());
            }
        });

        // Botão chegar no final
        btnChegarFinal.setOnClickListener(v -> {
            if (destino != null && marcadorUsuario != null) {
                marcadorUsuario.setPosition(destino);
                mapController.setCenter(destino);
                atualizarTempoEDistancia(destino);
                btnConcluir.setEnabled(true);
                btnConcluir.setTextColor(getColor(R.color.preto));
                btnConcluir.setBackgroundTintList(getColorStateList(R.color.branco));
            }
        });

        carregarRotaAnterior();
    }

    private void iniciarLocalizacao() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return; // Saia silenciosamente se a permissão ainda não foi concedida
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 2, locationListener);
    }

    private void pararLocalizacao() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(locationListener);
        }
    }

    private void carregarRotaAnterior() {
        // Aqui você pode recuperar a rota da tela anterior via Intent,
        // ou reprocessar a mesma rota. Vamos assumir por enquanto que o destino
        // está fixo em algum ponto de teste.
        destino = new GeoPoint(-23.564, -46.654); // Substitua por destino real
        Marker markerDestino = new Marker(mapView);
        markerDestino.setPosition(destino);
        markerDestino.setTitle("Destino");
        markerDestino.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(markerDestino);
    }

    private void atualizarTempoEDistancia(GeoPoint posAtual) {
        if (destino == null) return;

        float[] results = new float[1];
        Location.distanceBetween(
                posAtual.getLatitude(), posAtual.getLongitude(),
                destino.getLatitude(), destino.getLongitude(),
                results
        );
        float distanciaMetros = results[0];
        float tempoMinutos = (distanciaMetros / 1000f) / 40f * 60f; // 40km/h médio

        txtDistancia.setText(String.format("%.1f km", distanciaMetros / 1000));
        txtTempo.setText(String.format("%.0f min", tempoMinutos));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pararLocalizacao();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            iniciarLocalizacao();
        } else {
            // Permissão negada - tratar o caso
        }
    }
}
