package br.com.fecap.projetopi_tracktracker;

import android.Manifest;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;

public class CadastroPassFoto extends AppCompatActivity {

    //dados anteriores
    private String nome, sobrenome, cpf, dataNasc, email, telefone, endereco, senha;

    private static final int pedirCamera = 100;
    private static final int pedirGaleria = 101;
    private static final int pedirPermissoes = 102;
    private Bitmap imagemSelecionada;

    ImageView imagePreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_pass_foto);

        Intent intent = getIntent();

        nome = intent.getStringExtra("nome");
        sobrenome = intent.getStringExtra("sobrenome");
        cpf = intent.getStringExtra("cpf");
        dataNasc = intent.getStringExtra("dataNasc");
        email = intent.getStringExtra("email");
        telefone = intent.getStringExtra("telefone");
        endereco = intent.getStringExtra("endereco");
        senha = intent.getStringExtra("senha");

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
                imagePreview.setImageBitmap(photo);
                imagemSelecionada = photo; // <- aqui salva a imagem
            } else if (requestCode == pedirGaleria) {
                Uri selectedImage = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    imagePreview.setImageBitmap(bitmap);
                    imagemSelecionada = bitmap; // <- aqui também salva a imagem
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Erro ao carregar imagem", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "Nenhuma imagem selecionada", Toast.LENGTH_SHORT).show();
        }
    }


    public void voltar(View view){
        finish();
    }

    public class UsuarioComImagem {
        public String nome, sobrenome, cpf, dataNasc, email, telefone, endereco, senha;
        public byte[] imagem;
    }

    public UsuarioComImagem getDadosParaInserir() {
        if (imagemSelecionada == null) {
            Toast.makeText(this, "Imagem ainda não selecionada!", Toast.LENGTH_SHORT).show();
            return null;
        }

        UsuarioComImagem usuario = new UsuarioComImagem();
        usuario.nome = nome;
        usuario.sobrenome = sobrenome;
        usuario.cpf = cpf;
        usuario.dataNasc = dataNasc;
        usuario.email = email;
        usuario.telefone = telefone;
        usuario.endereco = endereco;
        usuario.senha = senha;
        usuario.imagem = bitmapParaBytes(imagemSelecionada);

        return usuario;
    }

    private byte[] bitmapParaBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }


}