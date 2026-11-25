package com.lensclick.app.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lensclick.app.data.local.LensClickDatabase
import com.lensclick.app.data.local.LensClickRepository
import com.lensclick.app.data.remote.auth.InMemorySessionStore
import com.lensclick.app.data.remote.auth.MobileAuthApi
import com.lensclick.app.data.remote.auth.MobileAuthRepository
import com.lensclick.app.data.remote.auth.MobileAuthRequest
import com.lensclick.app.data.remote.auth.MobileLoginResult
import com.lensclick.app.data.remote.auth.MobileRegisterRequest
import com.lensclick.app.domain.Conversation
import com.lensclick.app.domain.ConversationsGateway
import com.lensclick.app.domain.Favorite
import com.lensclick.app.domain.FavoritesGateway
import com.lensclick.app.domain.Message
import com.lensclick.app.domain.MobileSession
import com.lensclick.app.domain.MobileUser
import com.lensclick.app.domain.PublicPhotographer
import com.lensclick.app.domain.PublicPhotographerContacts
import com.lensclick.app.domain.PublicPhotographerPage
import com.lensclick.app.domain.PublicPhotographerProfile
import com.lensclick.app.domain.PublicPhotographersGateway
import com.lensclick.app.domain.PublicReviews
import com.lensclick.app.domain.Review
import com.lensclick.app.domain.ReviewRequest
import com.lensclick.app.domain.ReviewSummary
import com.lensclick.app.domain.ReviewsGateway
import com.lensclick.app.ui.theme.LensClickAppTheme
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhotographerRankingTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var database: LensClickDatabase
    private val photographer = PublicPhotographer(
        slug = "ana-ranking", name = "Ana do Ranking", city = "Recife", state = "PE",
        specialties = listOf("portrait"), startingPriceCents = null, ratingAverage = 4.95,
        reviewCount = 12
    )
    private val unrated = photographer.copy(
        slug = "perfil-sem-nota", name = "Perfil sem nota", ratingAverage = null, reviewCount = 0
    )

    private fun openApp(ranked: List<PublicPhotographer>, published: List<PublicPhotographer> = listOf(unrated)) {
        database = Room.inMemoryDatabaseBuilder(rule.activity, LensClickDatabase::class.java).build()
        val photographers = object : PublicPhotographersGateway {
            override suspend fun list(city: String, specialty: String?, page: Int) =
                PublicPhotographerPage(published, page, published.size, 1)

            override suspend fun listTopRated(page: Int) = PublicPhotographerPage(ranked, page, ranked.size, 1)

            override suspend fun get(slug: String): PublicPhotographerProfile {
                val profile = (ranked + published).first { it.slug == slug }
                return PublicPhotographerProfile(
                    profile = profile, bio = "Retratos com luz natural", serviceRadiusKm = null,
                    availability = "available", instagram = "", website = "", profilePic = "",
                    contacts = PublicPhotographerContacts("", ""), portfolio = emptyList(), isPublished = true
                )
            }
        }
        val reviews = object : ReviewsGateway {
            override suspend fun getReviews(photographerSlug: String): PublicReviews {
                if (photographerSlug == DemoPhotographerSlug) return PublicReviews(ReviewSummary(0, null), emptyList())
                check(photographerSlug == photographer.slug)
                return PublicReviews(
                    ReviewSummary(12, 4.95, mapOf(5 to 11, 4 to 1)),
                    listOf(Review(1, "Cliente do ensaio", 5, "As fotos ficaram ótimas.", "2026-11-20"))
                )
            }

            override suspend fun submitReview(photographerSlug: String, rating: Int, comment: String): Boolean =
                error("Unexpected review submission")
            override suspend fun requestVerification(photographerSlug: String, message: String): Unit =
                error("Unexpected verification request")
            override suspend fun listReviewRequests(): List<ReviewRequest> = emptyList()
            override suspend fun resolveReviewRequest(id: Long, confirm: Boolean): Unit =
                error("Unexpected review verification")
        }
        val client = MobileUser(17, "Cliente", "cliente@example.test", null, "client", true, false)
        val api = object : MobileAuthApi {
            override suspend fun login(request: MobileAuthRequest) =
                MobileLoginResult.Success(MobileSession(client, "portfolio-test-token", "2099-01-01T00:00:00Z"))
            override suspend fun register(request: MobileRegisterRequest): MobileSession = error("Unexpected registration")
            override suspend fun currentUser(accessToken: String): MobileUser = client
            override suspend fun logout(accessToken: String) = Unit
        }
        val favoriteRows = mutableListOf<Favorite>()
        val favorites = object : FavoritesGateway {
            override suspend fun list() = favoriteRows.toList()
            override suspend fun add(slug: String) {
                check(slug == photographer.slug)
                favoriteRows.add(Favorite(52, slug, photographer.name, "2026-11-24"))
            }
            override suspend fun remove(photographerId: Long) {
                favoriteRows.removeAll { it.photographerId == photographerId }
            }
        }
        val conversationRows = mutableListOf<Conversation>()
        val conversations = object : ConversationsGateway {
            override suspend fun list() = conversationRows.toList()
            override suspend fun getMessages(conversationId: Long) =
                listOf(Message(1, client.id, conversationRows.single().lastMessage, "2026-11-24", true))
            override suspend fun sendMessage(conversationId: Long, text: String): Message = error("Unexpected message")
            override suspend fun startConversation(photographerSlug: String, text: String): Conversation {
                check(photographerSlug == photographer.slug && conversationRows.isEmpty())
                return Conversation(43, photographer.name, text, "2026-11-24", photographerSlug = photographerSlug)
                    .also(conversationRows::add)
            }
            override suspend fun markAsRead(conversationId: Long) = Unit
        }
        val factory = viewModelFactory {
            initializer {
                LensClickViewModel(
                    LensClickRepository(database.lensClickDao()),
                    publicPhotographers = photographers,
                    reviewsGateway = reviews,
                    conversationsGateway = conversations,
                    favoritesGateway = favorites,
                    mobileAuthRepository = MobileAuthRepository(api, InMemorySessionStore())
                )
            }
        }
        rule.setContent { LensClickAppTheme { LensClickApp(viewModel(factory = factory)) } }
        rule.onNodeWithText("Explorar fotógrafos").performScrollTo().performClick()
        awaitText("Buscar Fotógrafos")
    }

    @After fun closeDatabase() {
        if (::database.isInitialized) database.close()
    }

    @Test fun rankingShowsScoreAndOpensPublishedReviewsAsGuest() {
        openApp(listOf(photographer, unrated))
        val discovery = rule.onNodeWithTag("search-discovery")
        discovery.performScrollToNode(hasText("Fotógrafos mais bem avaliados"))
        rule.onNodeWithText("Fotógrafos mais bem avaliados").assertIsDisplayed()
        discovery.performScrollToNode(hasText("Ana do Ranking"))
        rule.onNodeWithContentDescription("1º lugar").assertIsDisplayed()
        rule.onNodeWithText("4,95 de 5 · 12 avaliações").assertIsDisplayed()

        rule.onNodeWithText("Ana do Ranking").performClick()
        awaitText("As fotos ficaram ótimas.")
        rule.onNodeWithText("★ 4,95 de 5 · 12 avaliações").assertIsDisplayed()
        rule.onNodeWithText("Contratar").assertIsDisplayed()
        rule.onNodeWithText("Solicitar orçamento").assertIsDisplayed()
        rule.onNodeWithText("Distribuição das notas").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("As fotos ficaram ótimas.").performScrollTo().assertIsDisplayed()
        rule.onNodeWithContentDescription("Voltar").performScrollTo().performClick()
        rule.onNodeWithTag("search-discovery").performScrollToNode(hasText("Fotógrafos mais bem avaliados"))
    }

    @Test fun publishedDemoUsesLabeledGalleryAndReviewsWhenItsPortfolioIsEmpty() {
        val demo = unrated.copy(slug = DemoPhotographerSlug, name = "Fotógrafo da demo")
        openApp(emptyList(), listOf(demo))
        rule.onNodeWithTag("search-discovery").performScrollToNode(hasText(demo.name))
        rule.onNodeWithText(demo.name).performClick()
        awaitText("Fotos de demonstração")
        rule.onNodeWithText("Solicitar orçamento").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Favoritar").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Conversar").performScrollTo().assertIsDisplayed()
        rule.onNodeWithContentDescription("Noivos sorrindo depois da cerimônia.").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Avaliações fictícias de demonstração").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Isabela Campos").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Sem avaliações").performScrollTo().assertIsDisplayed()
    }

    @Test fun hireWithoutPublishedPriceResumesTheQuoteAfterLogin() {
        openApp(listOf(photographer))
        rule.onNodeWithTag("search-discovery").performScrollToNode(hasText("Ana do Ranking"))
        rule.onNodeWithText("Ana do Ranking").performClick()
        awaitText("Contratar")
        rule.onNodeWithText("Contratar").performScrollTo().performClick()
        signIn()
        awaitText("Contratar com orçamento")
        rule.onNodeWithText(
            "Peça um orçamento para definir o valor. Depois de aceitar a proposta, você poderá escolher como pagar."
        ).performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Selecionar data prevista").performScrollTo().performClick()
        chooseEventDate(2090, 6, 15)
        rule.onNodeWithText("Confirmar").performScrollTo().performClick()
        val selected = "Data prevista · ${displayCalendarDate("2090-06-15")}"
        rule.onNodeWithText(selected).performScrollTo().assertIsDisplayed()
        rule.onNodeWithText(selected).performClick()
        rule.onNodeWithText("Cancelar").performScrollTo().performClick()
        rule.onNodeWithText(selected).performScrollTo().assertIsDisplayed()
    }

    @Test fun portfolioSavesFavoritesAndOpensTheSameChatAgain() {
        openApp(listOf(photographer))
        rule.onNodeWithTag("search-discovery").performScrollToNode(hasText("Ana do Ranking"))
        rule.onNodeWithText("Ana do Ranking").performClick()
        awaitText("Favoritar")
        rule.onNodeWithText("Favoritar").performScrollTo().performClick()
        signIn()
        awaitText("Favoritado")
        rule.onNodeWithText("Favoritado").performScrollTo().performClick()
        awaitText("Favoritar")

        rule.onNodeWithText("Conversar").performScrollTo().performClick()
        awaitText("Conversar com Ana do Ranking")
        rule.onNodeWithContentDescription("Mensagem").performTextInput("Olá, podemos conversar sobre o evento?")
        rule.onNodeWithText("Enviar").performClick()
        awaitText("Conversa via LensClick")
        awaitText("Olá, podemos conversar sobre o evento?")
        rule.onNodeWithContentDescription("Voltar").performClick()
        awaitText("Conversar")
        rule.onNodeWithText("Conversar").performScrollTo().performClick()
        awaitText("Conversa via LensClick")
        rule.onNodeWithText("Conversar com Ana do Ranking").assertDoesNotExist()
    }

    private fun signIn() {
        awaitText("Bem-vindo de volta!")
        rule.onNodeWithContentDescription("E-mail").performTextInput("cliente@example.test")
        rule.onNodeWithContentDescription("Senha").performTextInput("SenhaTeste1")
        rule.onNodeWithText("Entrar").performScrollTo().performClick()
    }

    @Test fun homeShowsAnInlineEmptyStateWhenNoReviewsArePublished() {
        openApp(listOf(unrated))
        rule.onNodeWithContentDescription("Início").performClick()
        awaitText("Seu momento, seu fotógrafo")
        rule.onNodeWithTag("home-discovery").performScrollToNode(hasText("Ainda não há fotógrafos com avaliações publicadas."))
        rule.onNodeWithText("Ainda não há fotógrafos com avaliações publicadas.").assertIsDisplayed()
        rule.onNodeWithText("Ranking de avaliações").assertDoesNotExist()
    }

    private fun awaitText(text: String) {
        rule.waitUntil(10_000) { rule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty() }
    }
}
