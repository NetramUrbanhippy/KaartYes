package nl.kaartyes.app.ui.screens.photos

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nl.kaartyes.app.data.repository.CardRepository
import nl.kaartyes.app.domain.model.Card
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class PhotosViewModel @Inject constructor(
    private val repository: CardRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _card = MutableStateFlow<Card?>(null)
    val card: StateFlow<Card?> = _card.asStateFlow()

    fun loadCard(id: Long) {
        viewModelScope.launch {
            _card.value = repository.getCard(id)
        }
    }

    fun saveFrontPhoto(cardId: Long, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val path = copyImageToStorage(uri, "front_$cardId") ?: return@launch
            val current = _card.value ?: return@launch
            val updated = current.copy(frontImagePath = path)
            repository.updateCard(updated)
            _card.value = updated
        }
    }

    fun saveBackPhoto(cardId: Long, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val path = copyImageToStorage(uri, "back_$cardId") ?: return@launch
            val current = _card.value ?: return@launch
            val updated = current.copy(backImagePath = path)
            repository.updateCard(updated)
            _card.value = updated
        }
    }

    fun removeFrontPhoto(cardId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = _card.value ?: return@launch
            current.frontImagePath?.let { deleteFile(it) }
            val updated = current.copy(frontImagePath = null)
            repository.updateCard(updated)
            _card.value = updated
        }
    }

    fun removeBackPhoto(cardId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = _card.value ?: return@launch
            current.backImagePath?.let { deleteFile(it) }
            val updated = current.copy(backImagePath = null)
            repository.updateCard(updated)
            _card.value = updated
        }
    }

    private fun copyImageToStorage(uri: Uri, prefix: String): String? {
        return try {
            val dir = File(context.filesDir, "card_photos").also { it.mkdirs() }
            val file = File(dir, "$prefix.jpg")
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            inputStream.use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    private fun deleteFile(path: String) {
        File(path).takeIf { it.exists() }?.delete()
    }
}
