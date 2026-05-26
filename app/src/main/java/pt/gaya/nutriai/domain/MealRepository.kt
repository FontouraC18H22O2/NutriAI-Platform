package pt.gaya.nutriai.domain

/**
 * Contrato que define todas as operações de dados permitidas para as refeições.
 * Segue o princípio de inversão de dependência (Clean Architecture).
 */
interface MealRepository {
    // 'suspend' indica que esta função corre em background sem bloquear o ecrã do telemóvel
    suspend fun getMealsByUserId(userId: String): List<Meal>
    suspend fun saveMeal(meal: Meal): Boolean
}