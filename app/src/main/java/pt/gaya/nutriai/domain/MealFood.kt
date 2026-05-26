package pt.gaya.nutriai.domain

/**
 * Representa um alimento que faz parte de uma refeição.
 */
data class MealFood(
    val id: String,
    val mealId: String,
    val foodName: String,
    val portionGrams: Double,
    val calories: Int,
    val proteins: Double,
    val carbs: Double,
    val fats: Double,
    val wasDetectedByAi: Boolean
)