package br.com.lensclick.app.ui.screens.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.lensclick.app.data.remote.AccountRepository
import br.com.lensclick.app.data.remote.ApiException
import br.com.lensclick.app.data.remote.ProfessionalProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfessionalProfileUiState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val bio: String = "",
    val specialties: List<String> = emptyList(),
    val city: String = "",
    val state: String = "",
    val serviceRadiusKm: String = "",
    val startingPriceReais: String = "",
    val availability: String = "available",
    val publicEmail: String = "",
    val publicPhone: String = "",
    val instagram: String = "",
    val website: String = "",
    val showEmail: Boolean = false,
    val showPhone: Boolean = false,
    val isPublished: Boolean = false,
    val savedMessage: String? = null,
    val error: String? = null
) {
    val publishHint: String
        get() = when {
            isPublished -> "Seu perfil está visível na busca pública."
            else -> "Para publicar: foto, apresentação (60+ caracteres), especialidade, cidade/UF e ao menos um contato público."
        }
}

class ProfessionalProfileViewModel(private val repo: AccountRepository = AccountRepository()) : ViewModel() {

    private val _state = MutableStateFlow(ProfessionalProfileUiState())
    val state: StateFlow<ProfessionalProfileUiState> = _state

    private var radiusKmValue: Int? = null
    private var priceCentsValue: Long? = null
    private var loadedOnce = false

    fun load() {
        if (loadedOnce) return
        loadedOnce = true
        viewModelScope.launch {
            try {
                apply(repo.getProfessionalProfile())
                _state.value = _state.value.copy(loading = false)
            } catch (e: ApiException) {
                _state.value = _state.value.copy(loading = false, error = e.message)
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = "Não foi possível conectar. Verifique sua internet.")
            }
        }
    }

    private fun apply(p: ProfessionalProfile) {
        radiusKmValue = p.serviceRadiusKm?.toInt()
        priceCentsValue = p.startingPriceCents
        _state.value = ProfessionalProfileUiState(
            loading = _state.value.loading,
            bio = p.bio,
            specialties = p.specialties,
            city = p.city,
            state = p.state,
            serviceRadiusKm = p.serviceRadiusKm?.let { it.toInt().toString() } ?: "",
            startingPriceReais = p.startingPriceCents?.let { formatReais(it) } ?: "",
            availability = p.availability.ifBlank { "available" },
            publicEmail = p.publicEmail,
            publicPhone = p.publicPhone,
            instagram = p.instagram,
            website = p.website,
            showEmail = p.showEmail,
            showPhone = p.showPhone,
            isPublished = p.isPublished
        )
    }

    fun onBioChange(v: String) {
        if (v.length <= 1400) update { it.copy(bio = v) }
    }

    fun toggleSpecialty(value: String) {
        update {
            val current = it.specialties.toMutableList()
            if (value in current) current.remove(value)
            else if (current.size < 6) current.add(value)
            it.copy(specialties = current)
        }
    }

    fun onCityChange(v: String) { if (v.length <= 80) update { it.copy(city = v) } }

    fun onStateChange(v: String) { update { it.copy(state = v.uppercase()) } }

    fun onRadiusChange(v: String) {
        if (!v.all(Char::isDigit) || v.length > 4) return
        radiusKmValue = v.toIntOrNull()
        update { it.copy(serviceRadiusKm = v) }
    }

    fun onPriceChange(v: String) {
        // Aceita "1500", "1500,50" ou "1500.50" — converte para centavos.
        if (!v.matches(Regex("\\d{0,7}([.,]\\d{0,2})?"))) return
        priceCentsValue = v.replace(",", ".")
            .split(".")
            .let { parts ->
                val reais = parts[0].toLongOrNull() ?: 0L
                val cents = when (parts.getOrNull(1)) {
                    null -> 0L
                    else -> parts[1].padEnd(2, '0').toLongOrNull() ?: 0L
                }
                if (reais > 9_999_999) null else reais * 100 + cents
            }
        update { it.copy(startingPriceReais = v) }
    }

    fun onAvailabilityChange(v: String) { update { it.copy(availability = v) } }
    fun onPublicEmailChange(v: String) { if (v.length <= 120) update { it.copy(publicEmail = v.trim()) } }
    fun onPublicPhoneChange(v: String) {
        if (v.length <= 20 && v.all { c -> c.isDigit() || c in "+()- " }) update { it.copy(publicPhone = v) }
    }
    fun onInstagramChange(v: String) {
        if (v.length <= 30 && v.matches(Regex("@?[A-Za-z0-9._]*"))) update { it.copy(instagram = v.removePrefix("@")) }
    }
    fun onWebsiteChange(v: String) { if (v.length <= 300) update { it.copy(website = v) } }
    fun onShowEmailChange(v: Boolean) { update { it.copy(showEmail = v) } }
    fun onShowPhoneChange(v: Boolean) { update { it.copy(showPhone = v) } }

    /** Liga/desliga publicação imediatamente; a validação real acontece no backend ao salvar. */
    fun onPublishedChange(v: Boolean) { update { it.copy(isPublished = v) } }

    /** Atalho do botão: inverte a publicação e salva em seguida. */
    fun togglePublish() {
        _state.value = _state.value.copy(isPublished = !_state.value.isPublished)
        submit()
    }

    private fun update(t: (ProfessionalProfileUiState) -> ProfessionalProfileUiState) {
        _state.value = t(_state.value).copy(error = null, savedMessage = null)
    }

    fun submit() {
        val s = _state.value
        if (s.saving) return
        when {
            s.bio.trim().length > 1200 -> return fail("A apresentação deve ter no máximo 1200 caracteres.")
            s.state.isNotBlank() && !Regex("^[A-Z]{2}$").matches(s.state) ->
                return fail("Informe a UF com 2 letras (ex.: SP).")
            s.publicPhone.isNotBlank() && s.publicPhone.filter(Char::isDigit).length < 8 ->
                return fail("Telefone profissional inválido.")
            s.instagram.isNotBlank() && !Regex("^[A-Za-z0-9._]{1,30}$").matches(s.instagram) ->
                return fail("Instagram inválido (use apenas letras, números, ponto e underline).")
            s.specialties.size > 6 -> return fail("Escolha no máximo 6 especialidades.")
        }
        viewModelScope.launch {
            _state.value = s.copy(saving = true)
            try {
                val saved = repo.updateProfessionalProfile(
                    ProfessionalProfile(
                        bio = s.bio.trim(),
                        specialties = s.specialties,
                        city = s.city.trim(),
                        state = s.state.trim(),
                        serviceRadiusKm = radiusKmValue?.toDouble(),
                        startingPriceCents = priceCentsValue,
                        availability = s.availability,
                        publicEmail = s.publicEmail.trim(),
                        publicPhone = s.publicPhone.trim(),
                        instagram = s.instagram.trim(),
                        website = s.website.trim(),
                        showEmail = s.showEmail && s.publicEmail.isNotBlank(),
                        showPhone = s.showPhone && s.publicPhone.isNotBlank(),
                        isPublished = s.isPublished
                    )
                )
                apply(saved)
                _state.value = _state.value.copy(saving = false, savedMessage = "Perfil salvo com sucesso.")
            } catch (e: ApiException) {
                _state.value = _state.value.copy(saving = false, error = e.message)
            } catch (e: Exception) {
                _state.value = _state.value.copy(saving = false, error = "Não foi possível conectar. Verifique sua internet.")
            }
        }
    }

    private fun fail(msg: String) { _state.value = _state.value.copy(error = msg, saving = false) }

    private fun formatReais(cents: Long): String {
        val reais = cents / 100
        val rem = cents % 100
        return if (rem == 0L) reais.toString() else String.format("%d.%02d", reais, rem)
    }
}
