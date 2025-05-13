package br.fecap.pi.TrackTracker;

import android.graphics.Color;
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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
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
    private static final String GRAPHHOPPER_API_KEY = "0f35c7de-f736-442f-8f23-c6abd354a5f9";
    private Polyline rotaAtual;
    private static final int LOCATION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_corrida_pass_em_andamento);

        Configuration.getInstance().load(getApplicationContext(), androidx.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapController = mapView.getController();
        mapController.setZoom(18.0);

        double latOrigem = getIntent().getDoubleExtra("lat_origem", 0.0);
        double lonOrigem = getIntent().getDoubleExtra("lon_origem", 0.0);
        double latDestino = getIntent().getDoubleExtra("lat_destino", 0.0);
        double lonDestino = getIntent().getDoubleExtra("lon_destino", 0.0);


        String tempo = getIntent().getStringExtra("tempo");
        String distancia = getIntent().getStringExtra("distancia");

        destino = new GeoPoint(latDestino, lonDestino);
        GeoPoint origem = new GeoPoint(latOrigem, lonOrigem);
        calcularERemostrarRotaComApi(origem, destino);

        // Marcador inicial do usuário
        marcadorUsuario = new Marker(mapView);
        marcadorUsuario.setPosition(origem);
        marcadorUsuario.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marcadorUsuario.setTitle("Você está aqui");
        mapView.getOverlays().add(marcadorUsuario);

        // Marcador de destino
        Marker marcadorDestino = new Marker(mapView);
        marcadorDestino.setPosition(destino);
        marcadorDestino.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marcadorDestino.setTitle("Destino");
        mapView.getOverlays().add(marcadorDestino);

        mapController.setCenter(origem);
        mapView.invalidate();

        // Inicia views
        txtDistancia = findViewById(R.id.txtDistância);
        txtTempo = findViewById(R.id.txtTempo);
        btnCentralizar = findViewById(R.id.btnCentralizar);
        btnConcluir = findViewById(R.id.btnConcluirCorr);
        btnDesvio = findViewById(R.id.btnAtivaDetec);
        btnChegarFinal = findViewById(R.id.btnChegarNoFinal);
        imgMicrofone = findViewById(R.id.imgMicroIc);

        // Exibir informações
        txtTempo.setText(tempo);
        txtDistancia.setText(distancia);

        // Botões
        btnCentralizar.setOnClickListener(v -> {
            if (marcadorUsuario != null) {
                mapView.getController().setZoom(18.0);
                mapView.getController().animateTo(marcadorUsuario.getPosition()); // dá um efeito mais fluido e confiável
            }
        });

        // Iniciar rastreamento da localização em tempo real
        iniciarLocalizacaoTempoReal();


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

    }

    private void calcularERemostrarRotaComApi(GeoPoint origem, GeoPoint destino){
        String url = "https://graphhopper.com/api/1/route?point=" + origem.getLatitude() + "," + origem.getLongitude() +
                "&point=" + destino.getLatitude() + "," + destino.getLongitude() +
                "&vehicle=car&locale=pt&points_encoded=false&key=" + GRAPHHOPPER_API_KEY;

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();

        okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();

       client.newCall(request).enqueue(new okhttp3.Callback(){
           @Override
           public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e){
               runOnUiThread(() -> android.widget.Toast.makeText(CorridaPassEmAndamento.this, "Erro ao obter rota", android.widget.Toast.LENGTH_SHORT).show());
           }

           @Override
           public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException{
               if(response.isSuccessful()){
                   String responseData = response.body().string();
                   try{
                       org.json.JSONObject json = new org.json.JSONObject(responseData);
                       org.json.JSONArray paths = json.getJSONArray("paths");
                       org.json.JSONObject path = paths.getJSONObject(0);

                       double distanciaMetros = path.getDouble("distance");
                       double duracaoSegundos = path.getDouble("time")/1000.0;

                       org.json.JSONArray points = path.getJSONObject("points").getJSONArray("coordinates");

                       List<GeoPoint> geoPoints = new ArrayList<>();
                       for(int i = 0; i<points.length(); i++){
                           org.json.JSONArray coord = points.getJSONArray(i);
                           geoPoints.add(new GeoPoint(coord.getDouble(1), coord.getDouble(0)));
                       }

                       runOnUiThread(()->{
                           if(rotaAtual != null){
                               mapView.getOverlays().remove(rotaAtual);
                           }
                           rotaAtual = new Polyline();
                           rotaAtual.setPoints(geoPoints);
                           rotaAtual.setColor(Color.BLUE);
                           rotaAtual.setWidth(8f);
                           mapView.getOverlays().add(rotaAtual);
                           mapView.invalidate();
                       });
                   } catch (org.json.JSONException e){
                       runOnUiThread(()-> android.widget.Toast.makeText(CorridaPassEmAndamento.this, "Erro ao processar rota", Toast.LENGTH_SHORT).show());
                   }
               }
           }
       });
    }

    private void desenharRota(List<GeoPoint> pontosRota) {

        Polyline linhaRota = new Polyline();
        linhaRota.setPoints(pontosRota);
        linhaRota.setColor(Color.BLUE);
        linhaRota.setWidth(5f);
        mapView.getOverlays().add(linhaRota);
        mapView.invalidate();
    }

    private void iniciarLocalizacaoTempoReal() {
        FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        locationClient.requestLocationUpdates(
                com.google.android.gms.location.LocationRequest.create()
                        .setInterval(3000)
                        .setFastestInterval(2000)
                        .setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY),
                new com.google.android.gms.location.LocationCallback() {
                    @Override
                    public void onLocationResult(com.google.android.gms.location.LocationResult locationResult) {
                        if (locationResult != null) {
                            Location novaLocalizacao = locationResult.getLastLocation();
                            GeoPoint novaPosicao = new GeoPoint(novaLocalizacao.getLatitude(), novaLocalizacao.getLongitude());
                            marcadorUsuario.setPosition(novaPosicao);
                            mapView.invalidate();
                        }
                    }
                },
                null
        );
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

    private void atualizarTempoEDistancia(GeoPoint posAtual) {
        if (destino == null) return;

        String url = "https://graphhopper.com/api/1/route?point=" + posAtual.getLatitude() + "," + posAtual.getLongitude() +
                "&point=" + destino.getLatitude() + "," + destino.getLongitude() +
                "&vehicle=car&locale=pt&points_encoded=false&key=" + GRAPHHOPPER_API_KEY;

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(CorridaPassEmAndamento.this, "Erro ao atualizar distância", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        org.json.JSONObject json = new org.json.JSONObject(responseData);
                        org.json.JSONArray paths = json.getJSONArray("paths");
                        org.json.JSONObject path = paths.getJSONObject(0);

                        double distanciaMetros = path.getDouble("distance");
                        double duracaoSegundos = path.getDouble("time") / 1000.0;

                        String distanciaStr = String.format(Locale.US, "%.1fkm", distanciaMetros / 1000.0);
                        String tempoStr = String.format(Locale.US, "%.0fmin", duracaoSegundos / 60.0);

                        runOnUiThread(() -> {
                            txtDistancia.setText(distanciaStr);
                            txtTempo.setText(tempoStr);
                        });

                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(CorridaPassEmAndamento.this, "Erro ao processar tempo/distância", Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
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
