import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    @Multipart
    @POST("/cadastro") // Altere conforme sua rota real
    Call<Void> cadastrarUsuario(
            @Part("nome") RequestBody nome,
            @Part("sobrenome") RequestBody sobrenome,
            @Part("cpf") RequestBody cpf,
            @Part("dataNasc") RequestBody dataNasc,
            @Part("email") RequestBody email,
            @Part("telefone") RequestBody telefone,
            @Part("endereco") RequestBody endereco,
            @Part("senha") RequestBody senha,
            @Part MultipartBody.Part imagem
    );
}
