package nl.kaartyes.app.ui.screens.carddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nl.kaartyes.app.data.repository.CardRepository
import nl.kaartyes.app.domain.model.BarcodeFormat
import nl.kaartyes.app.domain.model.Card
import javax.inject.Inject

@HiltViewModel
class CardDetailViewModel @Inject constructor(
    private val repository: CardRepository
) : ViewModel() {

    private val _card = MutableStateFlow<Card?>(null)
    val card: StateFlow<Card?> = _card.asStateFlow()

    private val _showQr = MutableStateFlow(false)
    val showQr: StateFlow<Boolean> = _showQr.asStateFlow()

    private val _showFavoriteLimitDialog = MutableStateFlow(false)
    val showFavoriteLimitDialog: StateFlow<Boolean> = _showFavoriteLimitDialog.asStateFlow()

    fun loadCard(id: Long) {
        viewModelScope.launch {
            val loaded = repository.getCard(id)
            _card.value = loaded
            _showQr.value = loaded?.barcodeFormat == BarcodeFormat.QR_CODE
            if (loaded != null) repository.recordUsage(id)
        }
    }

    fun toggleDisplayFormat() {
        _showQr.value = !_showQr.value
    }

    fun toggleFavorite() {
        val current = _card.value ?: return
        viewModelScope.launch {
            if (!current.isFavorite && repository.getFavoriteCount() >= 4) {
                _showFavoriteLimitDialog.value = true
                return@launch
            }
            val updated = current.copy(isFavorite = !current.isFavorite)
            repository.updateCard(updated)
            _card.value = updated
        }
    }

    fun dismissFavoriteLimitDialog() {
        _showFavoriteLimitDialog.value = false
    }

    fun deleteCard(id: Long, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteCard(id)
            onDeleted()
        }
    }
}
