package com.example.lensclickapp.data

data class Photographer(
    val name: String,
    val specialty: String,
    val initials: String,
    val price: String
)

data class Budget(
    val title: String,
    val date: String,
    val status: String,
    val info: String
)

object LensClickData {
    val photographers = listOf(
        Photographer("Mariana Lopes", "Casamentos", "ML", "R$ 2.500"),
        Photographer("Lucas Almeida", "Ensaios", "LA", "R$ 800"),
        Photographer("Carlos Nobre", "Eventos", "CN", "R$ 1.200"),
        Photographer("Juliana Reis", "Ensaios Femininos", "JR", "R$ 700")
    )

    val budgets = listOf(
        Budget("Casamento", "24/06/2025 • São Paulo, SP", "Aguardando", "3 respostas"),
        Budget("Ensaio Pré Wedding", "10/07/2025 • Campinas, SP", "Proposta recebida", "R$ 2.200"),
        Budget("Aniversário 15 anos", "19/06/2025 • São Paulo, SP", "Aceito", "R$ 1.500"),
        Budget("Evento Corporativo", "20/05/2025 • São Paulo, SP", "Recusado", "Carlos Nobre")
    )
}
