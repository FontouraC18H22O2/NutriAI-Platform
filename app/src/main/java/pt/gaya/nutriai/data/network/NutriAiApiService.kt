package pt.gaya.nutriai.data.network

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface NutriAiApiService {

    // Define o endpoint relativo que vai receber a imagem do prato
    @Multipart
    @POST("api/meals/analyze")
    suspend fun analyzeMealPhoto(
        // O '@Part' indica que estamos a enviar um pedaço (part) do formulário multipart, que neste caso é o ficheiro binário da foto
        @Part photo: MultipartBody.Part
    ): AnalysisResponse
}