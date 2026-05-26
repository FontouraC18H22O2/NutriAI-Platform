package pt.gaya.nutriai.domain

import java.util.Date

/**
 * Representa o utilizador core dentro da lógica do nosso negócio.
 * Mapeia diretamente os dados essenciais da tabela 'users'.
 */
data class User(
    val id: String,
    val email: String,
    val createdAt: Date = Date()
)