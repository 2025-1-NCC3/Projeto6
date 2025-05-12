package br.fecap.pi.TrackTracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import br.fecap.pi.TrackTracker.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import okhttp3.*;

import org.json.*;

public class CorridaPass extends AppCompatActivity {

    private MapView mapView;
    private EditText etBarraDePesquisa;
    private GeoPoint localAtual;
    private FusedLocationProviderClient locationClient;
    private Marker marcadorPesquisa;
    private Button btnLimpar;
    private BottomSheetBehavior bottomSheetBehavior;
    private TextView tvDistancia, tvTempo, tvPreco;
    private Button btnCancelar, btnConfirmar;

    private Handler recenterHandler = new Handler();
    private Runnable recenterRunnable;
    private Polyline rotaAtual;
    private GeoPoint pontoCentralizado;
    private boolean estaAtiva = true;



    private long ultimaInteracao = 0;

    private static final int REQUEST_PERMISSIONS = 1;
    private static final String GRAPHHOPPER_API_KEY = "0f35c7de-f736-442f-8f23-c6abd354a5f9"; // Substitua pela sua chave

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corrida_pass);
        Log.d("DebugGabriel", "onCreate iniciado");

        Configuration.getInstance().load(getApplicationContext(), androidx.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        mapView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                ultimaInteracao = System.currentTimeMillis();
                cancelarRecentralizacao();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                agendarRecentralizacao();
            }
            return false;
        });

        etBarraDePesquisa = findViewById(R.id.etBarraDePesquisa);
        btnLimpar = findViewById(R.id.btnLimparPesquisa);

        etBarraDePesquisa.setOnEditorActionListener((v, actionId, event) -> {
            String endereco = etBarraDePesquisa.getText().toString();
            if (!endereco.isEmpty()) {
                buscarEndereco(endereco);
            }
            return true;
        });

        btnLimpar.setOnClickListener(v -> limparPesquisa());

        locationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS);
        } else {
            mostrarLocalizacaoAtual();
        }

        tvDistancia = findViewById(R.id.txtDistância);
        tvTempo = findViewById(R.id.txtTempo);
        tvPreco = findViewById(R.id.txtPreco);
        btnConfirmar = findViewById(R.id.btnConfirmar);
        btnCancelar = findViewById(R.id.btnCancelar);

        btnConfirmar.setEnabled(false);
        btnConfirmar.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.cinzaHint));

        btnCancelar.setOnClickListener(v -> {
            cancelarRecentralizacao();  // Cancela agendamento de recentralização
            finish();                   // Finaliza atividade
        });

    }

    private void buscarEndereco(String endereco) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> resultados = geocoder.getFromLocationName(endereco, 1);
            if (!resultados.isEmpty()) {
                Address local = resultados.get(0);
                GeoPoint ponto = new GeoPoint(local.getLatitude(), local.getLongitude());

                if (marcadorPesquisa != null) {
                    mapView.getOverlays().remove(marcadorPesquisa);
                }

                marcadorPesquisa = new Marker(mapView);
                marcadorPesquisa.setPosition(ponto);
                marcadorPesquisa.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marcadorPesquisa.setTitle("Destino");
                mapView.getOverlays().add(marcadorPesquisa);

                IMapController controller = mapView.getController();
                controller.setZoom(18.0);
                controller.setCenter(ponto);
                pontoCentralizado = ponto;
                mapView.invalidate();

                if (localAtual != null) {
                    calcularRota(localAtual, ponto);
                }


                agendarRecentralizacao();

            } else {
                Toast.makeText(this, "Endereço não encontrado", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao buscar o endereço", Toast.LENGTH_SHORT).show();
        }
    }

    private void calcularRota(GeoPoint origem, GeoPoint destino) {
        String url = "https://graphhopper.com/api/1/route?point=" + origem.getLatitude() + "," + origem.getLongitude() +
                "&point=" + destino.getLatitude() + "," + destino.getLongitude() +
                "&vehicle=car&locale=pt&points_encoded=false&key=" + GRAPHHOPPER_API_KEY;

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(CorridaPass.this, "Erro ao calcular rota", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    try {
                        JSONObject json = new JSONObject(responseData);
                        JSONArray paths = json.getJSONArray("paths");
                        JSONObject path = paths.getJSONObject(0);

                        double distanciaMetros = path.getDouble("distance");
                        double duracaoSegundos = path.getDouble("time") / 1000.0;

                        JSONArray points = path.getJSONObject("points").getJSONArray("coordinates");

                        List<GeoPoint> geoPoints = new java.util.ArrayList<>();
                        for (int i = 0; i < points.length(); i++) {
                            JSONArray coord = points.getJSONArray(i);
                            geoPoints.add(new GeoPoint(coord.getDouble(1), coord.getDouble(0)));
                        }

                        double distanciaKm = distanciaMetros / 1000.0;
                        double preco = 4.0 + (distanciaKm * 2.0); // Fórmula simples

                        String distanciaStr = String.format(Locale.US, "%.1fkm", distanciaKm);
                        String tempoStr = String.format(Locale.US, "%.0fmin", duracaoSegundos / 60.0);
                        String precoStr = String.format(Locale.US, "R$%.2f", preco);

                        runOnUiThread(() -> {
                            if (!estaAtiva) return;
                            if (rotaAtual != null) {
                                mapView.getOverlays().remove(rotaAtual);
                            }

                            Polyline rota = new Polyline();
                            rota.setPoints(geoPoints);
                            rota.setColor(Color.BLUE);
                            rota.setWidth(10f);
                            rotaAtual = rota;
                            mapView.getOverlays().add(rotaAtual);
                            mapView.invalidate();

                            tvDistancia.setText(distanciaStr);
                            tvTempo.setText(tempoStr);
                            tvPreco.setText(precoStr);

                            btnConfirmar.setEnabled(true);
                            btnConfirmar.setBackgroundTintList(ContextCompat.getColorStateList(CorridaPass.this, R.color.azulClaro));
                        });

                    } catch (JSONException e) {
                        runOnUiThread(() -> Toast.makeText(CorridaPass.this, "Erro ao interpretar rota", Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }


    private void limparPesquisa() {
        if (rotaAtual != null) {
            mapView.getOverlays().remove(rotaAtual);
            rotaAtual = null;
        }
        if (marcadorPesquisa != null) {
            mapView.getOverlays().remove(marcadorPesquisa);
            marcadorPesquisa = null;
            mapView.invalidate();
        }
        tvDistancia.setText("00.0km");
        tvTempo.setText("00min");
        tvPreco.setText("R$00.00");
        btnConfirmar.setEnabled(false);
        btnConfirmar.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.cinzaHint));

        etBarraDePesquisa.setText("");
        localAtual = null;
        mostrarLocalizacaoAtual(); // isso define localAtual para a posição do usuário e centraliza
        pontoCentralizado = localAtual; // Recentraliza no usuário
        agendarRecentralizacao();
    }


    private void mostrarLocalizacaoAtual() {
        pontoCentralizado = localAtual;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return; // Saia silenciosamente se a permissão ainda não foi concedida
        }
        locationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        localAtual = new GeoPoint(location.getLatitude(), location.getLongitude());

                        IMapController mapController = mapView.getController();
                        mapController.setZoom(18.0);
                        mapController.setCenter(localAtual);

                        Marker marcador = new Marker(mapView);
                        marcador.setPosition(localAtual);
                        marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        marcador.setTitle("Você está aqui");
                        mapView.getOverlays().add(marcador);
                        mapView.invalidate();

                        Log.d("DebugGabriel", "Localização exibida com sucesso");
                    } else {
                        Toast.makeText(this, "Localização indisponível", Toast.LENGTH_SHORT).show();
                        Log.d("DebugGabriel", "Localização nula");
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mostrarLocalizacaoAtual();
        } else {
            Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show();
        }
    }

    private void agendarRecentralizacao() {
        recenterRunnable = () -> {
            if (pontoCentralizado != null) {
                IMapController controller = mapView.getController();
                controller.setZoom(18.0);
                controller.setCenter(pontoCentralizado);
                Log.d("DebugGabriel", "Mapa recentralizado automaticamente");
            }
        };
        recenterHandler.postDelayed(recenterRunnable, 5000); // 5 segundos
    }

    private void cancelarRecentralizacao() {
        if (recenterRunnable != null) {
            recenterHandler.removeCallbacks(recenterRunnable);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        estaAtiva = false;
        if (mapView != null) {
            mapView.onPause();
            mapView.onDetach(); // IMPORTANTE para liberar recursos e evitar crashes
        }

        if (rotaAtual != null) {
            mapView.getOverlays().remove(rotaAtual);
            rotaAtual = null;
        }

        if (marcadorPesquisa != null) {
            mapView.getOverlays().remove(marcadorPesquisa);
            marcadorPesquisa = null;
        }


        cancelarRecentralizacao(); // Cancela o handler também
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelarRecentralizacao();
    }



}
