package nl.kaartyes.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import nl.kaartyes.app.data.repository.CardRepository
import nl.kaartyes.app.domain.model.Card
import nl.kaartyes.app.domain.model.SortOrder
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CardRepository
) : ViewModel() {

    private val _sortOrder = MutableStateFlow(SortOrder.A_Z)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _favoriteLimitReached = MutableStateFlow(false)
    val favoriteLimitReached: StateFlow<Boolean> = _favoriteLimitReached.asStateFlow()

    private val allCardsFlow = repository.getCards(SortOrder.A_Z)

    val cards: StateFlow<List<Card>> = combine(
        allCardsFlow, _sortOrder, _searchQuery
    ) { allCards, sortOrder, query ->
        val filtered = if (query.isBlank()) allCards
        else allCards.filter { card ->
            card.storeName.contains(query, ignoreCase = true) ||
            card.nickname.contains(query, ignoreCase = true) ||
            card.note.contains(query, ignoreCase = true)
        }
        filtered.sortedWith(
            compareByDescending<Card> { it.isFavorite }
                .then(
                    if (sortOrder == SortOrder.A_Z) compareBy { it.displayName.lowercase() }
                    else compareByDescending { it.displayName.lowercase() }
                )
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favoriteCount: StateFlow<Int> = allCardsFlow
        .map { list -> list.count { it.isFavorite } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearFavoriteLimitMessage() {
        _favoriteLimitReached.value = false
    }

    fun deleteCard(cardId: Long) {
        viewModelScope.launch {
            repository.deleteCard(cardId)
        }
    }
}
