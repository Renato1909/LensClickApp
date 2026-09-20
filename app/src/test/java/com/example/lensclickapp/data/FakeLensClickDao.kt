package com.example.lensclickapp.data

import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory test double; SQL constraints and transactions are tested on real Room. */
class FakeLensClickDao : LensClickDao {
    val photographerRows = MutableStateFlow<List<Photographer>>(emptyList())
    val budgetRows = MutableStateFlow<List<Budget>>(emptyList())
    val users = mutableListOf<User>()

    override fun observePhotographers() = photographerRows
    override fun observeBudgets() = budgetRows
    override suspend fun photographerCount() = photographerRows.value.size
    override suspend fun budgetCount() = budgetRows.value.size

    override suspend fun insertPhotographers(photographers: List<Photographer>) {
        photographers.forEach { insertPhotographer(it) }
    }

    override suspend fun insertPhotographer(photographer: Photographer): Long {
        val id = photographerRows.value.size.toLong() + 1
        photographerRows.value += photographer.copy(id = id)
        return id
    }

    override suspend fun insertBudget(budget: Budget): Long {
        val id = budgetRows.value.size.toLong() + 1
        budgetRows.value += budget.copy(id = id)
        return id
    }

    override suspend fun insertBudgets(budgets: List<Budget>) {
        budgets.forEach { insertBudget(it) }
    }

    override suspend fun insertUser(user: User): Long {
        if (users.any { it.email == user.email }) return -1L
        val id = users.size.toLong() + 1
        users += user.copy(id = id)
        return id
    }

    override suspend fun findUser(email: String) = users.find { it.email == email }
}
