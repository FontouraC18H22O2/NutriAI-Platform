package pt.gaya.nutriai.data.network

import com.google.gson.annotations.SerializedName

data class AnalysisResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("meal_type") val mealType: String,
    @SerializedName("total_calories") val totalCalories: Int,
    @SerializedName("protein") val protein: Double,
    @SerializedName("carbs") val carbs: Double,
    @SerializedName("fats") val fats: Double,
    @SerializedName("detected_foods") val detectedFoods: List<String>
)