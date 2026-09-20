package com.example.lensclickapp.data

import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

/** Local prototype only. A future mobile API contract must replace this demo session. */
class LensClickRepository(private val dao: LensClickDao) {
    // Registration credentials live only for this process; Room stores profiles, never passwords.
    private val demoPasswordHashes = ConcurrentHashMap<String, String>().apply {
        put("seu@email.com", hashPassword("lensclick"))
    }
    val photographers = dao.observePhotographers()
    val budgets = dao.observeBudgets()

    suspend fun seedDemoData() {
        if (dao.photographerCount() == 0) dao.insertPhotographers(LensClickData.photographers)
        if (dao.budgetCount() == 0) dao.insertBudgets(LensClickData.budgets)
        dao.insertUser(
            User(
                name = "Usuário de demonstração",
                email = "seu@email.com",
                role = "client"
            )
        )
    }

    suspend fun register(name: String, email: String, password: String): User? {
        val user = User(
            name = name.trim(),
            email = email.trim().lowercase(),
            role = "client"
        )
        val id = dao.insertUser(user)
        if (id == -1L) return null
        demoPasswordHashes[user.email] = hashPassword(password)
        return user.copy(id = id)
    }

    suspend fun registerPhotographer(
        name: String,
        email: String,
        password: String,
        specialty: String,
        city: String,
        price: String,
        bio: String
    ): User? {
        val user = User(
            name = name.trim(),
            email = email.trim().lowercase(),
            role = "photographer"
        )
        val id = dao.insertPhotographerAccount(
            user = user,
            photographer = Photographer(
                name = name.trim(),
                specialty = specialty.trim(),
                initials = initialsFor(name),
                price = price.trim(),
                city = city.trim(),
                bio = bio.trim()
            )
        )
        if (id == -1L) return null
        demoPasswordHashes[user.email] = hashPassword(password)
        return user.copy(id = id)
    }

    suspend fun authenticate(email: String, password: String): User? {
        val user = dao.findUser(email.trim().lowercase()) ?: return null
        return user.takeIf { demoPasswordHashes[it.email] == hashPassword(password) }
    }

    suspend fun createBudget(
        type: String,
        date: String,
        place: String,
        duration: String,
        description: String,
        additionalInfo: String
    ) {
        dao.insertBudget(
            Budget(
                title = type.trim(),
                date = "${date.trim()} • ${place.trim()}",
                status = "Aguardando",
                info = duration.trim(),
                description = description.trim(),
                additionalInfo = additionalInfo.trim()
            )
        )
    }

    private fun hashPassword(password: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray())
            .joinToString("") { "%02x".format(it) }

    private fun initialsFor(name: String): String = name.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
}
