package br.com.fecap.projetopi_tracktracker;

import android.Manifest;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class CadastroPassFoto extends AppCompatActivity {

    private static final int pedirCamera = 100;
    private static final int pedirGaleria = 101;
    private static final int pedirPermissoes = 102;

    ImageView imagePreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_pass_foto);

        imagePreview = findViewById(R.id.imgPreview);
        Button btnCamera = findViewById(R.id.btnCamera);
        Button btnGaleria = findViewById(R.id.btnGaleria);


        // Verifica e solicita permissões
        if (!checkPermissions()) {
            requestPermissions();
        }

        // Abrir câmera
        btnCamera.setOnClickListener(v -> {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(cameraIntent, pedirCamera);
            }
        });

        // Abrir galeria
        btnGaleria.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, pedirGaleria);
        });
    }

    // Verifica se permissões já foram dadas
    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    // Solicita permissões necessárias
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                pedirPermissoes);
    }

    // Resultado das escolhas de câmera ou galeria
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == pedirCamera) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                imagePreview.setImageBitmap(photo); // mostra imagem tirada
            } else if (requestCode == pedirGaleria) {
                Uri selectedImage = data.getData();
                imagePreview.setImageURI(selectedImage); // mostra imagem da galeria
            }
        } else {
            Toast.makeText(this, "Nenhuma imagem selecionada", Toast.LENGTH_SHORT).show();
        }
    }
}