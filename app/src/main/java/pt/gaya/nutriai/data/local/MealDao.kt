package pt.gaya.nutriai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {

    // Insere uma nova refeição. Se houver conflito de ID (raro), substitui os dados
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity)

    // Vai buscar todas as refeições ordenadas pela mais recente primeiro
    // Usamos Flow para que o Compose atualize o ecrã do histórico instantaneamente se algo mudar!
    @Query("SELECT * FROM meals ORDER BY timestamp DESC")
    fun getAllMeals(): Flow<List<MealEntity>>

    // Função opcional caso o utilizador queira apagar um registo do histórico
    @Query("DELETE FROM meals WHERE id = :mealId")
    suspend fun deleteMealById(mealId: Long)
}