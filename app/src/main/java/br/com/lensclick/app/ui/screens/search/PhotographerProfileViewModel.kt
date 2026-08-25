package br.com.lensclick.app.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.lensclick.app.data.model.PhotographerDetail
import br.com.lensclick.app.data.remote.ApiClient
import br.com.lensclick.app.data.remote.ApiException
import br.com.lensclick.app.data.remote.PhotographersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PhotographerProfileUiState(
    val profile: PhotographerDetail? = null,
    val loading: Boolean = false,
    val error: String? = null
)

class PhotographerProfileViewModel(
    private val repo: PhotographersRepository = PhotographersRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(PhotographerProfileUiState())
    val state: StateFlow<PhotographerProfileUiState> = _state

    fun load(slug: String) {
        if (_state.value.loading || _state.value.profile != null) return
        viewModelScope.launch {
            _state.value = PhotographerProfileUiState(loading = true)
            try {
                val response = repo.detail(slug)
                _state.value = PhotographerProfileUiState(profile = response.profile)
            } catch (e: ApiException) {
                _state.value = PhotographerProfileUiState(error = e.message)
            } catch (e: Exception) {
                _state.value = PhotographerProfileUiState(
                    error = "Não foi possível conectar. Verifique sua internet."
                )
            }
        }
    }

    /** URL absoluta da foto de perfil (a API devolve caminho relativo). */
    fun profilePictureUrl(): String? {
        val path = _state.value.profile?.profilePic?.takeIf { it.isNotBlank() } ?: return null
        return if (path.startsWith("http")) path else ApiClient.BASE_URL + path
    }

    /** URL absoluta de uma imagem do portfólio usando o imageUrl relativo devolvido pela API. */
    fun portfolioImageUrl(item: br.com.lensclick.app.data.model.PortfolioItem): String =
        if (item.imageUrl.startsWith("http")) item.imageUrl else ApiClient.BASE_URL + item.imageUrl
}
