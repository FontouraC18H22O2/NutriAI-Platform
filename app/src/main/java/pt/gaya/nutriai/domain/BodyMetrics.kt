package pt.gaya.nutriai.domain

import java.util.Date

enum class Gender { MALE, FEMALE, OTHER }

enum class ActivityLevel { SEDENTARY, LIGHT, MODERATE, ACTIVE }

enum class FitnessGoal { FAT_LOSS, MAINTENANCE, MUSCLE_GAIN }

/**
 * Representa o estado físico e os objetivos do utilizador.
 * Essencial para o motor de IA calcular o plano alimentar personalizado.
 */
data class BodyMetrics(
    val id: String,
    val userId: String,
    val weightKg: Double,
    val heightCm: Double,
    val age: Int,
    val gender: Gender,
    val activityLevel: ActivityLevel,
    val goal: FitnessGoal,
    val recordedAt: Date = Date()
)