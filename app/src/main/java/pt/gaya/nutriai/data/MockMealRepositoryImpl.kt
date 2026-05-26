package pt.gaya.nutriai.data

import kotlinx.coroutines.delay
import pt.gaya.nutriai.domain.Meal
import pt.gaya.nutriai.domain.MealFood
import pt.gaya.nutriai.domain.MealRepository
import pt.gaya.nutriai.domain.MealType
import java.util.Date

/**
 * Implementação simulada do repositório para o MVP.
 * Simula latência de rede e devolve dados idênticos ao MySQL local.
 */
class MockMealRepositoryImpl : MealRepository {

    override suspend fun getMealsByUserId(userId: String): List<Meal> {
        // Simula o tempo que o telemóvel demoraria a ir ao servidor via Wi-Fi/4G (1.5 segundos)
        delay(1500)

        // Criamos os alimentos falsos simulando o que guardámos no HeidiSQL
        val arroz = MealFood(
            id = "FOOD-001", mealId = "MEAL-001", foodName = "Arroz Branco Cozido",
            portionGrams = 150.0, calories = 195, proteins = 3.75, carbs = 42.0, fats = 0.30,
            wasDetectedByAi = true
        )

        val frango = MealFood(
            id = "FOOD-002", mealId = "MEAL-001", foodName = "Bife de Frango Grelhado",
            portionGrams = 150.0, calories = 247, proteins = 48.0, carbs = 0.0, fats = 5.5,
            wasDetectedByAi = true
        )

        // Montamos a refeição completa
        val almocoSimulado = Meal(
            id = "MEAL-001",
            userId = userId,
            consumedAt = Date(),
            mealType = MealType.LUNCH,
            foods = listOf(arroz, frango),
            imageUrl = "https://storage.nutriai.pt/refeicoes/2026/05/foto_almoco_001.jpg",
            aiConfidenceScore = 0.9450
        )

        return listOf(almocoSimulado)
    }

    override suspend fun saveMeal(meal: Meal): Boolean {
        delay(1000) // Simula o upload para o servidor
        return true // Diz à App que guardou com sucesso
    }
}