package com.example.lensclickapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.lensclickapp.LensClickApplication
import com.example.lensclickapp.data.LensClickRepository
import com.example.lensclickapp.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LensClickViewModel(private val repository: LensClickRepository) : ViewModel() {
    companion object {
        val Factory = viewModelFactory {
            initializer {
                LensClickViewModel((this[APPLICATION_KEY] as LensClickApplication).repository)
            }
        }
    }
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    val photographers = repository.photographers.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
    val budgets = repository.budgets.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    init {
        viewModelScope.launch { repository.seedDemoData() }
    }

    fun login(email: String, password: String, result: (User?) -> Unit) {
        viewModelScope.launch {
            val user = repository.authenticate(email, password)
            _currentUser.value = user
            result(user)
        }
    }

    fun register(name: String, email: String, password: String, result: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = repository.register(name, email, password)
            _currentUser.value = user
            result(user != null)
        }
    }

    fun registerPhotographer(
        name: String,
        email: String,
        password: String,
        specialty: String,
        city: String,
        price: String,
        bio: String,
        result: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val user = repository.registerPhotographer(name, email, password, specialty, city, price, bio)
            _currentUser.value = user
            result(user != null)
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    fun createBudget(
        type: String,
        date: String,
        place: String,
        duration: String,
        description: String,
        additionalInfo: String,
        done: () -> Unit
    ) {
        viewModelScope.launch {
            repository.createBudget(type, date, place, duration, description, additionalInfo)
            done()
        }
    }
}
