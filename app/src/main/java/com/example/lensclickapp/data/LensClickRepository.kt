package com.example.lensclickapp.data

import java.security.MessageDigest

class LensClickRepository(private val dao: LensClickDao) {
    val photographers = dao.observePhotographers()
    val budgets = dao.observeBudgets()

    suspend fun seedDemoData() {
        if (dao.photographerCount() == 0) dao.insertPhotographers(LensClickData.photographers)
        if (dao.budgetCount() == 0) dao.insertBudgets(LensClickData.budgets)
        dao.insertUser(
            User(
                name = "Usuário de demonstração",
                email = "seu@email.com",
                passwordHash = hashPassword("lensclick"),
                role = "client"
            )
        )
    }

    suspend fun register(name: String, email: String, password: String): User? {
        val user = User(
            name = name.trim(),
            email = email.trim().lowercase(),
            passwordHash = hashPassword(password),
            role = "client"
        )
        val id = dao.insertUser(user)
        return if (id == -1L) null else user.copy(id = id)
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
            passwordHash = hashPassword(password),
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
        return if (id == -1L) null else user.copy(id = id)
    }

    suspend fun authenticate(email: String, password: String): User? {
        val user = dao.findUser(email.trim().lowercase()) ?: return null
        return user.takeIf { it.passwordHash == hashPassword(password) }
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
