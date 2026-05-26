package pt.gaya.nutriai.domain

import java.util.Date

enum class MealType { BREAKFAST, LUNCH, DINNER, SNACK }

/**
 * Representa uma refeição completa realizada pelo utilizador.
 * Agrupa todos os alimentos consumidos e calcula os totais macro-nutricionais.
 */
data class Meal(
    val id: String,
    val userId: String,
    val consumedAt: Date = Date(),
    val mealType: MealType,
    val foods: List<MealFood> = emptyList(), // Lista de alimentos que compõem o prato
    val imageUrl: String? = null,            // Opcional, caso a refeição tenha sido registada com foto
    val aiConfidenceScore: Double? = null    // Opcional, guarda a confiança caso a IA tenha sido usada
) {
   // Coeficientes matemáticos automáticos (Getters calculados em tempo real)
    val totalCalories: Int get() = foods.sumOf { it.calories }
    val totalProteins: Double get() = foods.sumOf { it.proteins }
    val totalCarbs: Double get() = foods.sumOf { it.carbs }
    val totalFats: Double get() = foods.sumOf { it.fats }
}