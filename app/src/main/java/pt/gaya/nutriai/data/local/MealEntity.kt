package pt.gaya.nutriai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealType: String,
    val totalCalories: Int,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    val detectedFoods: String, // Vamos guardar os alimentos como uma string separada por vírgulas
    val timestamp: Long = System.currentTimeMillis() // Guarda o dia e a hora em que o utilizador comeu
)