package pt.gaya.nutriai.presentation.camera

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import pt.gaya.nutriai.data.local.AppDatabase
import pt.gaya.nutriai.data.local.MealEntity
import pt.gaya.nutriai.data.network.AnalysisResponse
import pt.gaya.nutriai.data.network.RetrofitClient
import java.io.File

// Estado MVI para sabermos o que está a acontecer com o upload da foto
sealed interface CameraUiState {
    object Idle : CameraUiState
    object Loading : CameraUiState
    data class Success(val data: AnalysisResponse) : CameraUiState
    data class Error(val message: String) : CameraUiState
}

class CameraViewModel : ViewModel() {

    // Guarda o estado atual da rede reativamente para o Compose ler
    var uiState: CameraUiState by mutableStateOf(CameraUiState.Idle)
        private set

    // Adicionamos o parâmetro `context` para a função conseguir aceder ao ficheiro SQLite local
    fun uploadMealPhoto(context: Context, photoPath: String) {
        // Altera o estado para "A Carregar..." para podermos mostrar um spinner/progresso no ecrã
        uiState = CameraUiState.Loading

        // Dispara uma Coroutine em segundo plano (Background Thread) para não travar a UI
        viewModelScope.launch {
            try {
                val file = File(photoPath)
                if (!file.exists()) {
                    uiState = CameraUiState.Error("Ficheiro da foto não encontrado localmente.")
                    return@launch
                }

                // Transforma o ficheiro físico .jpg num corpo de requisição binária (image/jpeg)
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())

                // Mapeia o ficheiro para o formato "multipart" exigido pelo parâmetro @Part da API
                val body = MultipartBody.Part.createFormData("photo", file.name, requestFile)

                // Faz a chamada real à internet através do Retrofit
                val response = RetrofitClient.apiService.analyzeMealPhoto(body)

                // ==========================================
                // 💾 GRAVAÇÃO AUTOMÁTICA NA BASE DE DADOS LOCAL
                // ==========================================
                try {
                    val mealDao = AppDatabase.getDatabase(context).mealDao()

                    val novaRefeicao = MealEntity(
                        mealType = response.mealType,
                        totalCalories = response.totalCalories,
                        protein = response.protein,
                        carbs = response.carbs,
                        fats = response.fats,
                        detectedFoods = response.detectedFoods.joinToString(", ")
                    )
                    mealDao.insertMeal(novaRefeicao)
                } catch (dbError: Exception) {
                    // Se a BD falhar por algum motivo, fazemos print mas não travamos a app
                    dbError.printStackTrace()
                }
                // ==========================================

                // Se correu bem, passa os dados macro nutricionais da IA para o ecrã!
                uiState = CameraUiState.Success(response)

            } catch (e: Exception) {
                e.printStackTrace()
                uiState = CameraUiState.Error("Falha ao comunicar com o servidor: ${e.localizedMessage}")
            }
        }
    }

    // Função auxiliar para resetar o estado quando o utilizador quiser tirar outra foto
    fun resetToIdle() {
        uiState = CameraUiState.Idle
    }
}