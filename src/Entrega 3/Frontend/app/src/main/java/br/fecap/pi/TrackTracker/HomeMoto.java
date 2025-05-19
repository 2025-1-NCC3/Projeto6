package br.fecap.pi.TrackTracker;

import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.File;
import java.util.*;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;
//import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.location.Location;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

import android.os.Handler;
import android.view.MotionEvent;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.views.overlay.MapEventsOverlay;

import br.fecap.pi.TrackTracker.R;

public class HomeMoto extends AppCompatActivity {

    private MapView map;
    private FusedLocationProviderClient fusedLocationClient;
    private GeoPoint currentLocation;
    private Handler handler = new Handler();
    private Runnable resetMapRunnable;

    private SurfaceView surfaceViewCamera;
    private SurfaceHolder surfaceHolderCamera;
    private android.hardware.Camera camera;

    private MediaRecorder mediaRecorder;
    private boolean gravando = false;
    private String videoPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_moto);

        //Configuração para o mapa funcionar
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        map = findViewById(R.id.mapView);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        //ponto de partida do celular do usuário
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                IMapController mapController = map.getController();
                mapController.setZoom(19.0);
                mapController.setCenter(currentLocation);

                // Criar e adicionar marcador
                org.osmdroid.views.overlay.Marker startMarker = new org.osmdroid.views.overlay.Marker(map);
                startMarker.setPosition(currentLocation);
                startMarker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM);
                startMarker.setTitle("Você está aqui");
                map.getOverlays().add(startMarker);
            }

            resetMapRunnable = () -> {
                map.getController().setZoom(19.0);
                map.getController().setCenter(currentLocation);
            };

            MapEventsOverlay overlayEventos = new MapEventsOverlay(new MapEventsReceiver() {
                @Override
                public boolean singleTapConfirmedHelper(GeoPoint p) {
                    restartMapResetTimer();
                    return false;
                }

                @Override
                public boolean longPressHelper(GeoPoint p) {
                    restartMapResetTimer();
                    return false;
                }
            });
            map.getOverlays().add(overlayEventos);

            // Detecta toques diretos no MapView (como movimentações ou zoom)
            map.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
                    restartMapResetTimer();
                }
                return false;
            });
        });
        surfaceViewCamera = findViewById(R.id.surfaceViewCamera);

        Button btnGravar = findViewById(R.id.btnGravar);
        btnGravar.setOnClickListener(v -> {
            if (!gravando) {
                startVideoRecording();
                btnGravar.setText("Parar Gravação");
            } else {
                stopVideoRecording();
                btnGravar.setText("Gravar Vídeo");
            }
        });

        surfaceHolderCamera = surfaceViewCamera.getHolder();

        surfaceHolderCamera.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                startCameraPreview();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (camera != null) {
                    camera.stopPreview();
                    camera.release();
                    camera = null;
                }
            }
        });

    }

    private void startCameraPreview() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1010);
            return;
        }

        try {
            camera = android.hardware.Camera.open(android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
            camera.setDisplayOrientation(90);
            camera.setPreviewDisplay(surfaceHolderCamera);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao iniciar câmera", Toast.LENGTH_SHORT).show();
        }
    }


    //depois de 7 segundos de inatividade o mapa irá voltar à posição inicial
    private void restartMapResetTimer(){
        handler.removeCallbacks(resetMapRunnable);
        handler.postDelayed(resetMapRunnable, 5000); //5 segundos
    }

    public void ViagemAceita(View view) {
        Intent intent = new Intent(this, PreViagem.class);
        startActivity(intent);
        finish();
    }
    private void startVideoRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 3000);
            return;
        }

        File videoDir = new File(getExternalFilesDir(android.os.Environment.DIRECTORY_MOVIES), "VideosHomeMoto");
        if (!videoDir.exists()) videoDir.mkdirs();

        videoPath = new File(videoDir, "video_" + System.currentTimeMillis() + ".mp4").getAbsolutePath();

        try {
            camera.unlock();
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setCamera(camera);
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setOutputFile(videoPath);
            mediaRecorder.setPreviewDisplay(surfaceHolderCamera.getSurface());
            mediaRecorder.setVideoFrameRate(30);
            mediaRecorder.setVideoSize(640, 480);
            mediaRecorder.setOrientationHint(90);
            mediaRecorder.prepare();
            mediaRecorder.start();

            gravando = true;
            Toast.makeText(this, "Gravação iniciada!", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao iniciar gravação", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopVideoRecording() {
        if (mediaRecorder != null && gravando) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;

                camera.lock();
                gravando = false;

                Toast.makeText(this, "Vídeo salvo em: " + videoPath, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(this, "Erro ao parar gravação", Toast.LENGTH_SHORT).show();
            }
        }
    }

}