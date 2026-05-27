package pt.gaya.nutriai.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // URL base configurada para apontar para o localhost do teu PC de desenvolvimento através do emulador Android
    private const val BASE_URL = "http://10.0.2.2:8000/"

    val apiService: NutriAiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Transforma JSON em Objetos Kotlin automaticamente
            .build()
            .create(NutriAiApiService::class.java)
    }
}