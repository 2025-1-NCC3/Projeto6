package br.fecap.pi.TrackTracker;

//importações
import android.content.Intent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.hardware.Camera; // API antiga, como você está usando

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.MediaRecorder;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.File;
import java.io.IOException;
import java.util.*;



public class CorridaPassEmAndamento extends AppCompatActivity {

    // gravação do video
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Button btnGravarVideo;
    private MediaRecorder videoRecorder;
    private android.hardware.Camera camera;
    private boolean gravandoVideo = false;
    private String videoPath;
   ;

    // Componentes do mapa e controle
    private MapView mapView;
    private IMapController mapController;

    private GeoPoint destino; // Coordenada final da corrida
    private Marker marcadorUsuario;// Marcador da posição atual do usuário
    private TextView txtDistancia, txtTempo;// TextViews para tempo e distância
    private Button btnCentralizar, btnConcluir, btnDesvio20, btnDesvio30, btnDesvio50, btnChegarFinal;// Botões da interface
    private ImageView imgMicrofone;// Ícone do microfone

    // Gerenciador e listener de localização
    private LocationManager locationManager;
    private LocationListener locationListener;

    private static final String GRAPHHOPPER_API_KEY = "0f35c7de-f736-442f-8f23-c6abd354a5f9";// Chave da API GraphHopper para rotas
    private Polyline rotaAtual;// Linha da rota no mapa
    private double distanciaInicial = 0;// Distância inicialmente calculada
    private boolean rastreamentoAtivo = true;// Flag para ativar/desativar rastreamento

    // Flags de controle de alertas por desvio
    private boolean notificou20 = false;
    private boolean notificou30 = false;
    private boolean notificou50 = false;

    private String numeroEmergencia = "11951443638"; //número gabriel para emergência;
    private MediaRecorder mediaRecorder;// Gravação de áudio
    private String audioFilePath;//Caminho do arquivo da gravação
    private static final int LOCATION_REQUEST_CODE = 1001; // Código de permissão para localização



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
        btnDesvio20 = findViewById(R.id.btnAtivaDetec20);
        btnDesvio30 = findViewById(R.id.btnAtivaDetec30);
        btnDesvio50 = findViewById(R.id.btnAtivaDetec50);
        btnChegarFinal = findViewById(R.id.btnChegarNoFinal);
        imgMicrofone = findViewById(R.id.imgMicroIc);
//
        surfaceView = findViewById(R.id.surfaceView);
        btnGravarVideo = findViewById(R.id.btnGravarVideo);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        btnGravarVideo.setOnClickListener(v -> {
            if (!gravandoVideo) {
                startVideoRecording();
            } else {
                stopVideoRecording();
            }
        });

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3001);
            }
        }

//

        btnDesvio20.setOnClickListener(v -> {
            if (!notificou20) {
                notificou20 = true;
                rastreamentoAtivo = false;
                exibirNotificacao("Atenção", "Simulação: Pequeno desvio de rota detectado.");
            }
        });

        btnDesvio30.setOnClickListener(v -> {
            if (!notificou30) {
                notificou30 = true;
                rastreamentoAtivo = false;
                vibrarAlerta();
                exibirNotificacao("Alerta de Segurança", "Simulação: Você está se afastando da rota.");
            }
        });

        btnDesvio50.setOnClickListener(v -> {
            if (!notificou50) {
                notificou50 = true;
                rastreamentoAtivo = false;
                vibrarAlerta();
                enviarSmsEmergencia(); // Lembre de ativar a permissão no Manifest e em tempo de execução
                exibirNotificacao("Alerta Máximo", "Simulação: Desvio crítico de rota detectado!");
            }
        });

        btnConcluir.setAlpha(1.0f); // Garante visibilidade
        btnConcluir.setOnClickListener(btn -> {
            stopAudioRecording();
            android.content.Intent intent = new android.content.Intent(CorridaPassEmAndamento.this, CorridaFinalPass.class);
            startActivity(intent);
            finish(); // Encerra a tela atual para evitar voltar com "voltar"
        });

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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 2002);
        }


        // Botão chegar no final
        btnChegarFinal.setOnClickListener(v -> {
            rastreamentoAtivo = false;

            if (destino != null && marcadorUsuario != null) {
                marcadorUsuario.setPosition(destino);
                mapController.setCenter(destino);
                atualizarTempoEDistancia(destino);
                mapController.setZoom(18.0);
                locationManager.removeUpdates(locationListener);
            }

            // 1. Remover rota do mapa
            if (rotaAtual != null) {
                mapView.getOverlays().remove(rotaAtual);
                rotaAtual = null;
                mapView.invalidate();
            }
        });

        startAudioRecording();

    }
//
private void startVideoRecording() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 3000);
        return;
    }

    File videoDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "VideosCorrida");
    if (!videoDir.exists()) videoDir.mkdirs();

    videoPath = new File(videoDir, "video_" + System.currentTimeMillis() + ".mp4").getAbsolutePath();

    camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
    camera.setDisplayOrientation(90);

    try {
        camera.unlock();

        videoRecorder = new MediaRecorder();
        videoRecorder.setCamera(camera);
        videoRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        videoRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        videoRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        videoRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        videoRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        videoRecorder.setOutputFile(videoPath);
        videoRecorder.setPreviewDisplay(surfaceHolder.getSurface());
        videoRecorder.setVideoFrameRate(30);
        videoRecorder.setVideoSize(640, 480);
        videoRecorder.setOrientationHint(90);
        videoRecorder.prepare();
        videoRecorder.start();

        gravandoVideo = true;
        btnGravarVideo.setText("Parar Vídeo");
        Toast.makeText(this, "Gravação de vídeo iniciada", Toast.LENGTH_SHORT).show();

    } catch (IOException e) {
        e.printStackTrace();
        Toast.makeText(this, "Erro ao iniciar gravação de vídeo", Toast.LENGTH_SHORT).show();
    }
}

    private void stopVideoRecording() {
        if (videoRecorder != null && gravandoVideo) {
            videoRecorder.stop();

            // Atualiza a Galeria com o novo vídeo
            Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            scanIntent.setData(Uri.fromFile(new File(videoPath)));
            sendBroadcast(scanIntent);

            videoRecorder.release();
            videoRecorder = null;

            camera.lock();
            camera.release();
            camera = null;

            gravandoVideo = false;
            btnGravarVideo.setText("Gravar Vídeo");
            Toast.makeText(this, "Vídeo salvo em: " + videoPath, Toast.LENGTH_LONG).show();
        }
    }

    //


    // Inicia a gravação de áudio, salvando o arquivo no armazenamento privado
    private void startAudioRecording(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 2000);
            return;
        }
        try{
            File audioDir = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "Gravacoes");
            if(!audioDir.exists()){
                audioDir.mkdirs();
            }

            audioFilePath = new File(audioDir, "gravacao_" + System.currentTimeMillis() + ".3gp").getAbsolutePath();

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(audioFilePath);
            mediaRecorder.prepare();
            mediaRecorder.start();

            imgMicrofone.setBackgroundColor(ContextCompat.getColor(this, R.color.verde));

            Log.d("Audio", "Gravação iniciada em: " + audioFilePath);
        }catch (IOException e){
            e.printStackTrace();
            Toast.makeText(this, "Erro ao iniciar a gravação de áudio", Toast.LENGTH_SHORT).show();
        }
    }

    // Encerra a gravação de áudio e libera recursos
    private void stopAudioRecording() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                Toast.makeText(this, "Gravação finalizada: " + audioFilePath, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        imgMicrofone.setBackgroundColor(ContextCompat.getColor(this, R.color.cinzaHint));
    }

    // Calcula e desenha rota entre origem e destino usando a API do GraphHopper
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

                       distanciaInicial = path.getDouble("distance");

                   } catch (org.json.JSONException e){
                       runOnUiThread(()-> android.widget.Toast.makeText(CorridaPassEmAndamento.this, "Erro ao processar rota", Toast.LENGTH_SHORT).show());
                   }
               }
           }
       });
    }

    // Inicia rastreamento contínuo da localização via FusedLocationProviderClient
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
                        if (locationResult != null && rastreamentoAtivo) {
                            Location novaLocalizacao = locationResult.getLastLocation();
                            GeoPoint novaPosicao = new GeoPoint(novaLocalizacao.getLatitude(), novaLocalizacao.getLongitude());
                            marcadorUsuario.setPosition(novaPosicao);
                            mapView.invalidate();
                            atualizarTempoEDistancia(novaPosicao);
                        }
                    }
                },
                null
        );
    }

    // Inicia atualização de localização pelo LocationManager padrão
    private void iniciarLocalizacao() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return; // Saia silenciosamente se a permissão ainda não foi concedida
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 2, locationListener);
    }

    // Interrompe as atualizações de localização
    private void pararLocalizacao() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(locationListener);
        }
    }

    // Atualiza informações de tempo e distância até o destino, além de detectar desvios
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

                            if (distanciaMetros <= 30) {
                                btnConcluir.setEnabled(true);
                                btnConcluir.setTextColor(getColor(R.color.preto));
                                btnConcluir.setBackgroundTintList(getColorStateList(R.color.branco));
                            }
                        });

                        double porcentagemDesvio = (distanciaMetros - distanciaInicial) / distanciaInicial;

                        if (porcentagemDesvio >= 0.5 && !notificou50) {
                            notificou50 = true;
                            enviarSmsEmergencia();
                            exibirNotificacao("Alerta Máximo", "Desvio crítico de rota detectado!");
                        } else if (porcentagemDesvio >= 0.3 && !notificou30) {
                            notificou30 = true;
                            vibrarAlerta();
                            exibirNotificacao("Alerta de Segurança", "Você está se afastando da rota.");
                        } else if (porcentagemDesvio >= 0.2 && !notificou20) {
                            notificou20 = true;
                            exibirNotificacao("Atenção", "Pequeno desvio de rota detectado.");
                        }

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
        } else if (requestCode == 2002 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permissão de SMS concedida", Toast.LENGTH_SHORT).show();
        } else if (requestCode == 2002) {
            Toast.makeText(this, "Permissão de SMS negada", Toast.LENGTH_SHORT).show();
        }
    }

    // Exibe notificação personalizada com som e vibração (para alertas de segurança)
    private void exibirNotificacao(String titulo, String mensagem) {
        String channelId = "desvio_channel_id";

        // Criar canal de notificação se necessário
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Desvio de rota";
            String description = "Notificações relacionadas a desvios de rota";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.enableVibration(true);

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            channel.setSound(soundUri, audioAttributes);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Verifica permissão para Android 13+
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_alert) // Ícone padrão do sistema
                .setContentTitle(titulo)
                .setContentText(mensagem)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)); // Som

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());
    }

    // Vibra o celular com padrão de alerta
    private void vibrarAlerta() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            VibrationEffect efeito = VibrationEffect.createWaveform(new long[]{0, 600, 200, 600}, -1);
            vibrator.vibrate(efeito);
        } else {
            vibrator.vibrate(new long[]{0, 600, 200, 600}, -1); // Padrão compatível com Android < 8
        }
    }

    // Envia SMS automático em caso de desvio crítico detectado
    private void enviarSmsEmergencia() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permissão para SMS não concedida", Toast.LENGTH_SHORT).show();
            return;
        }

        android.telephony.SmsManager sms = android.telephony.SmsManager.getDefault();
        String msg = "ALERTA: Desvio de rota acima de 50% detectado no trajeto.";
        sms.sendTextMessage(numeroEmergencia, null, msg, null, null);
    }
}
