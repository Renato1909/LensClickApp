package br.com.lensclick.app.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.lensclick.app.data.model.Photographer
import br.com.lensclick.app.data.remote.ApiException
import br.com.lensclick.app.data.remote.PhotographersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SearchUiState(
    val city: String = "",
    val state: String = "",
    val specialty: String? = null,
    val availability: String? = null,
    val results: List<Photographer> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val totalPages: Int = 1,
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val error: String? = null,
    val searched: Boolean = false
) {
    /** Especialidades válidas aceitas pelo backend (worker.mjs). */
    val specialties: List<String> = listOf(
        "wedding", "events", "portrait", "family", "corporate",
        "product", "fashion", "newborn", "nature", "real-estate"
    )
}

class SearchViewModel(private val repo: PhotographersRepository = PhotographersRepository()) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state

    fun onCityChange(v: String) { _state.value = _state.value.copy(city = v) }
    fun onStateChange(v: String) {
        if (v.length <= 2 && v.all { it.isLetter() }) _state.value = _state.value.copy(state = v)
    }
    fun onSpecialtyChange(v: String?) { _state.value = _state.value.copy(specialty = v) }
    fun onAvailabilityChange(v: String?) { _state.value = _state.value.copy(availability = v) }

    fun search(reset: Boolean = true) {
        val s = _state.value
        if (s.loading || (reset && s.loadingMore)) return
        viewModelScope.launch {
            if (!reset) _state.value = s.copy(loadingMore = true)
            else _state.value = s.copy(loading = true, error = null)
            try {
                val pageToLoad = if (reset) 1 else s.page + 1
                val response = repo.search(
                    city = _state.value.city.takeIf { it.isNotBlank() },
                    state = _state.value.state.takeIf { it.isNotBlank() && it.length == 2 },
                    specialty = _state.value.specialty,
                    availability = _state.value.availability,
                    page = pageToLoad
                )
                _state.value = _state.value.copy(
                    results = if (reset) response.photographers else s.results + response.photographers,
                    total = response.pagination.total,
                    page = response.pagination.page,
                    totalPages = response.pagination.totalPages,
                    loading = false,
                    loadingMore = false,
                    searched = true
                )
            } catch (e: ApiException) {
                _state.value = _state.value.copy(
                    loading = false, loadingMore = false, error = e.message, searched = reset
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false, loadingMore = false,
                    error = "Não foi possível conectar. Verifique sua internet.",
                    searched = reset
                )
            }
        }
    }

    fun loadNextPage() {
        val s = _state.value
        if (s.page < s.totalPages && !s.loading && !s.loadingMore) search(reset = false)
    }
}
