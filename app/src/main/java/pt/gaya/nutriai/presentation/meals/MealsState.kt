package pt.gaya.nutriai.presentation.meals

import pt.gaya.nutriai.domain.Meal

/**
 * Representa os estados possíveis do ecrã de histórico de refeições.
 */
sealed interface MealsState {
    object Loading : MealsState
    data class Success(val meals: List<Meal>) : MealsState
    data class Error(val message: String) : MealsState
}