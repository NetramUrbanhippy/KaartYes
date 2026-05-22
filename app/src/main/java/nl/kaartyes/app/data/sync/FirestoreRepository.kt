package nl.kaartyes.app.data.sync

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import nl.kaartyes.app.domain.model.BarcodeFormat
import nl.kaartyes.app.domain.model.Card
import javax.inject.Inject
import javax.inject.Singleton

// Firestore security rules (set in Firebase Console):
//   match /users/{userId}/cards/{cardId} {
//     allow read, write: if request.auth != null && request.auth.uid == userId;
//   }
@Singleton
class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private fun cardsCol() = auth.currentUser?.uid?.let {
        firestore.collection("users").document(it).collection("cards")
    }

    suspend fun pushCard(card: Card): String {
        val col = cardsCol() ?: return ""
        val docRef = if (card.syncId.isBlank()) col.document() else col.document(card.syncId)
        docRef.set(card.toFirestoreMap()).await()
        return docRef.id
    }

    suspend fun deleteCard(syncId: String) {
        val col = cardsCol() ?: return
        col.document(syncId).delete().await()
    }

    suspend fun fetchAllCards(): List<Card> {
        val col = cardsCol() ?: return emptyList()
        val snapshot = col.get().await()
        return snapshot.documents
            .filter { it.getBoolean("deleted") != true }
            .mapNotNull { it.toCard() }
    }

    private fun Card.toFirestoreMap(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>(
            "cardNumber" to cardNumber,
            "storeName" to storeName,
            "nickname" to nickname,
            "colorHex" to colorHex,
            "barcodeFormat" to barcodeFormat.name,
            "note" to note,
            "isFavorite" to isFavorite,
            "usageCount" to usageCount,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
        )
        map["textColorLight"] = textColorLight
        return map
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toCard(): Card? = runCatching {
        Card(
            syncId = id,
            cardNumber = getString("cardNumber") ?: return null,
            storeName = getString("storeName") ?: return null,
            nickname = getString("nickname") ?: "",
            colorHex = getString("colorHex") ?: "#1976D2",
            barcodeFormat = runCatching {
                BarcodeFormat.valueOf(getString("barcodeFormat") ?: "")
            }.getOrDefault(BarcodeFormat.CODE_128),
            note = getString("note") ?: "",
            isFavorite = getBoolean("isFavorite") ?: false,
            textColorLight = getBoolean("textColorLight"),
            usageCount = getLong("usageCount")?.toInt() ?: 0,
            createdAt = getLong("createdAt") ?: System.currentTimeMillis(),
            updatedAt = getLong("updatedAt") ?: System.currentTimeMillis()
        )
    }.getOrNull()
}
