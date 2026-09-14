package com.example.lensclickapp.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "photographers", indices = [Index(value = ["userId"], unique = true)])
data class Photographer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long? = null,
    val name: String,
    val specialty: String,
    val initials: String,
    val price: String,
    @ColumnInfo(defaultValue = "'São Paulo, SP'") val city: String = "São Paulo, SP",
    @ColumnInfo(defaultValue = "''") val bio: String = ""
)

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val date: String,
    val status: String,
    val info: String,
    val description: String = "",
    val additionalInfo: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    @ColumnInfo(defaultValue = "'client'") val role: String = "client",
    val createdAt: Long = System.currentTimeMillis()
)

object LensClickData {
    val photographers = listOf(
        Photographer(name = "Mariana Lopes", specialty = "Casamentos", initials = "ML", price = "R$ 2.500"),
        Photographer(name = "Lucas Almeida", specialty = "Ensaios", initials = "LA", price = "R$ 800"),
        Photographer(name = "Carlos Nobre", specialty = "Eventos", initials = "CN", price = "R$ 1.200"),
        Photographer(name = "Juliana Reis", specialty = "Ensaios Femininos", initials = "JR", price = "R$ 700")
    )

    val budgets = listOf(
        Budget(title = "Casamento", date = "24/06/2025 • São Paulo, SP", status = "Aguardando", info = "3 respostas"),
        Budget(title = "Ensaio Pré Wedding", date = "10/07/2025 • Campinas, SP", status = "Proposta recebida", info = "R$ 2.200"),
        Budget(title = "Aniversário 15 anos", date = "19/06/2025 • São Paulo, SP", status = "Aceito", info = "R$ 1.500"),
        Budget(title = "Evento Corporativo", date = "20/05/2025 • São Paulo, SP", status = "Recusado", info = "Carlos Nobre")
    )
}
