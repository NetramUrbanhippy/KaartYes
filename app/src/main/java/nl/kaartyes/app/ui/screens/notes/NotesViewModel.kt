package nl.kaartyes.app.ui.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nl.kaartyes.app.data.repository.CardRepository
import nl.kaartyes.app.domain.model.Card
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: CardRepository
) : ViewModel() {

    private val _card = MutableStateFlow<Card?>(null)
    val card: StateFlow<Card?> = _card.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    fun loadCard(id: Long) {
        viewModelScope.launch {
            _card.value = repository.getCard(id)
        }
    }

    fun saveNote(note: String, onSaved: () -> Unit) {
        val current = _card.value ?: return
        viewModelScope.launch {
            _isSaving.value = true
            try {
                repository.updateCard(current.copy(note = note))
                onSaved()
            } finally {
                _isSaving.value = false
            }
        }
    }
}
