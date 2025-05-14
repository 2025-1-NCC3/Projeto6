package br.fecap.pi.TrackTracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// Importações do Volley
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

// import br.fecap.pi.TrackTracker.R; // Certifique-se que R está sendo importado corretamente

public class LoginPass extends AppCompatActivity {

    private EditText etEmail, etSenha;
    private Button btnLogin; // Renomeado de btnSeguinte para clareza
    private RequestQueue requestQueue;
    private final String URL_BASE_BACKEND = "http://localhost:3000"; // Ex: "http://10.0.2.2:3000" para emulador
    private static final String TAG = "LoginPassActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_pass);

        // Inicializar Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        // Referenciar Views do Layout
        // !!! CONFIRME SE OS IDs ABAIXO CORRESPONDEM AO SEU LAYOUT activity_login_pass.xml !!!
        etEmail = findViewById(R.id.etEmail); // Corrigido para ID do XML
        etSenha = findViewById(R.id.etSenha); // Corrigido para ID do XML
        btnLogin = findViewById(R.id.btnSeguinte); // Usando o ID do seu botão existente

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnLogin.setOnClickListener(v -> {
            realizarLogin();
        });
    }

    private void realizarLogin() {
        String email = etEmail.getText().toString().trim();
        String senha = etSenha.getText().toString().trim();

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha email e senha.", Toast.LENGTH_LONG).show();
            return;
        }

        JSONObject loginPayload = new JSONObject();
        try {
            loginPayload.put("Email", email);
            loginPayload.put("Senha", senha);
        } catch (JSONException e) {
            Log.e(TAG, "Erro ao criar JSON para login", e);
            Toast.makeText(this, "Erro ao preparar dados para login.", Toast.LENGTH_SHORT).show();
            return;
        }

        String urlLoginP = URL_BASE_BACKEND + "/loginp";
        Toast.makeText(this, "Tentando fazer login...", Toast.LENGTH_SHORT).show();

        JsonObjectRequest requestLogin = new JsonObjectRequest(Request.Method.POST, urlLoginP, loginPayload,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Resposta de /loginp: " + response.toString());
                        try {
                            String message = response.getString("message");
                            // String userId = response.getString("userId"); // Você pode querer salvar o ID do usuário
                            // String nomeUsuario = response.getString("nome"); // E o nome para exibir
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

                            // Login bem-sucedido, navegar para a Home do Passageiro
                            Intent intent = new Intent(LoginPass.this, HomePassageiro.class);
                            // intent.putExtra("USER_ID", userId); // Opcional: passar dados para a próxima tela
                            // intent.putExtra("USER_NOME", nomeUsuario);
                            startActivity(intent);
                            finish(); // Finaliza a tela de login

                        } catch (JSONException e) {
                            Log.e(TAG, "Erro ao processar JSON da resposta de /loginp", e);
                            Toast.makeText(getApplicationContext(), "Erro ao processar resposta do servidor.", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Erro na requisição /loginp", error);
                        String mensagemErro = "Email ou senha inválidos."; // Mensagem padrão
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            try {
                                String responseBody = new String(error.networkResponse.data, "utf-8");
                                JSONObject data = new JSONObject(responseBody);
                                if (data.has("error")) {
                                    mensagemErro = data.getString("error");
                                }
                                Log.e(TAG, "Corpo do erro de /loginp: " + responseBody);
                            } catch (Exception e) {
                                Log.e(TAG, "Erro ao parsear corpo do erro de /loginp", e);
                            }
                        }
                        Toast.makeText(getApplicationContext(), "Erro no login: " + mensagemErro, Toast.LENGTH_LONG).show();
                    }
                }
        );
        requestQueue.add(requestLogin);
    }

    public void cadastro(View view) { // Mantido do seu código original
        Intent intent = new Intent(this, CadastroPassPessoal.class);
        startActivity(intent);
    }

    public void voltar(View view) { // Mantido do seu código original
        finish();
    }

    // Métodos de ciclo de vida mantidos do seu código original
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

