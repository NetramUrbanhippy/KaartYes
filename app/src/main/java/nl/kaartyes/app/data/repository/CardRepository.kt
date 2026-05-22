package nl.kaartyes.app.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import nl.kaartyes.app.data.db.CardDao
import nl.kaartyes.app.data.db.toDomain
import nl.kaartyes.app.data.db.toEntity
import nl.kaartyes.app.data.sync.FirestoreRepository
import nl.kaartyes.app.domain.model.Card
import nl.kaartyes.app.domain.model.SortOrder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardRepository @Inject constructor(
    private val cardDao: CardDao,
    private val firestoreRepository: FirestoreRepository,
    private val appScope: CoroutineScope
) {
    fun getCards(sortOrder: SortOrder = SortOrder.A_Z): Flow<List<Card>> {
        return when (sortOrder) {
            SortOrder.A_Z -> cardDao.getAllCardsSortedAZ()
            SortOrder.Z_A -> cardDao.getAllCardsSortedZA()
        }.map { list -> list.map { it.toDomain() } }
    }

    suspend fun getFavoriteCount(): Int = cardDao.getFavoriteCount()

    suspend fun getCard(id: Long): Card? = cardDao.getCardById(id)?.toDomain()

    suspend fun addCard(card: Card): Long {
        val existing = cardDao.getCardByCardNumber(card.cardNumber)
        if (existing != null) return existing.id

        val localId = cardDao.insertCard(card.toEntity())
        appScope.launch {
            runCatching {
                val syncId = firestoreRepository.pushCard(card.copy(id = localId))
                if (syncId.isNotBlank()) cardDao.updateSyncId(localId, syncId)
            }
        }
        return localId
    }

    suspend fun updateCard(card: Card) {
        val updatedCard = card.copy(updatedAt = System.currentTimeMillis())
        cardDao.updateCard(updatedCard.toEntity())
        appScope.launch { runCatching { firestoreRepository.pushCard(updatedCard) } }
    }

    suspend fun deleteCard(id: Long) {
        val entity = cardDao.getCardById(id)
        cardDao.deleteCardById(id)
        if (entity != null && entity.syncId.isNotBlank()) {
            appScope.launch { runCatching { firestoreRepository.deleteCard(entity.syncId) } }
        }
    }

    suspend fun recordUsage(id: Long) {
        cardDao.incrementUsageCount(id)
        appScope.launch {
            runCatching {
                val entity = cardDao.getCardById(id)
                if (entity != null && entity.syncId.isNotBlank()) {
                    firestoreRepository.pushCard(entity.toDomain())
                }
            }
        }
    }
}
