package nl.kaartyes.app.data.sync

import nl.kaartyes.app.data.db.CardDao
import nl.kaartyes.app.data.db.toDomain
import nl.kaartyes.app.data.db.toEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val cardDao: CardDao,
    private val firestoreRepository: FirestoreRepository
) {
    suspend fun syncOnLogin() {
        val remoteCards = runCatching { firestoreRepository.fetchAllCards() }
            .getOrDefault(emptyList())

        if (remoteCards.isEmpty()) {
            // Firestore has no cards: push all local cards to the cloud (first-time sync).
            val localCards = runCatching { cardDao.getAllCardsOnce() }.getOrDefault(emptyList())
            for (local in localCards) {
                runCatching {
                    val syncId = firestoreRepository.pushCard(local.toDomain())
                    if (syncId.isNotBlank()) cardDao.updateSyncId(local.id, syncId)
                }
            }
        } else {
            // Firestore has cards: cloud is the source of truth, replace local DB.
            runCatching { cardDao.deleteAllCards() }
            for (remote in remoteCards) {
                runCatching { cardDao.insertCard(remote.toEntity()) }
            }
        }
    }
}
