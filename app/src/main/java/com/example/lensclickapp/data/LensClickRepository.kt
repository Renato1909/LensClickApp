package com.example.lensclickapp.data

class LensClickRepository(private val dao: LensClickDao) {
    val photographers = dao.observePhotographers()
    val budgets = dao.observeBudgets()

    suspend fun seedDemoData() {
        if (dao.photographerCount() == 0) dao.insertPhotographers(LensClickData.photographers)
        if (dao.budgetCount() == 0) dao.insertBudgets(LensClickData.budgets)
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

}
