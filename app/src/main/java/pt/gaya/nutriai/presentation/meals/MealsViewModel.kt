package pt.gaya.nutriai.presentation.meals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.gaya.nutriai.data.MockMealRepositoryImpl
import pt.gaya.nutriai.domain.MealRepository

class MealsViewModel : ViewModel() {

    // Instanciamos o nosso repositório simulado diretamente por agora
    private val repository: MealRepository = MockMealRepositoryImpl()

    // O estado começa sempre em Modo de Carregamento (Loading)
    private val _uiState = MutableStateFlow<MealsState>(MealsState.Loading)
    val uiState: StateFlow<MealsState> = _uiState.asStateFlow()

    init {
        loadMeals()
    }

    fun loadMeals() {
        viewModelScope.launch {
            _uiState.value = MealsState.Loading
            try {
                // Vai buscar os dados simulados (demora 1.5s devido ao delay que programámos)
                val userMeals = repository.getMealsByUserId("USER-001")
                _uiState.value = MealsState.Success(userMeals)
            } catch (e: Exception) {
                _uiState.value = MealsState.Error("Não foi possível carregar as refeições.")
            }
        }
    }
}