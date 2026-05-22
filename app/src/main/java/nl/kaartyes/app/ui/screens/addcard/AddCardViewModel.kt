package nl.kaartyes.app.ui.screens.addcard

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
import androidx.compose.ui.graphics.toArgb
import nl.kaartyes.app.ui.theme.TileColors
import javax.inject.Inject

@HiltViewModel
class AddCardViewModel @Inject constructor(
    private val repository: CardRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun addCard(
        cardNumber: String,
        storeName: String,
        barcodeFormat: BarcodeFormat = BarcodeFormat.CODE_128,
        onSuccess: (Long) -> Unit
    ) {
        if (cardNumber.isBlank() || storeName.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val randomColor = TileColors.random()
                val colorHex = "#%06X".format(randomColor.toArgb() and 0xFFFFFF)
                val card = Card(
                    cardNumber = cardNumber.trim(),
                    storeName = storeName.trim(),
                    colorHex = colorHex,
                    barcodeFormat = barcodeFormat
                )
                val cardId = repository.addCard(card)
                onSuccess(cardId)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() { _error.value = null }
}
