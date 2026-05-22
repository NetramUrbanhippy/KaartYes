package nl.kaartyes.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {

    @Query("SELECT * FROM cards ORDER BY storeName ASC")
    fun getAllCards(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE id = :id")
    suspend fun getCardById(id: Long): CardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CardEntity): Long

    @Update
    suspend fun updateCard(card: CardEntity)

    @Delete
    suspend fun deleteCard(card: CardEntity)

    @Query("DELETE FROM cards WHERE id = :id")
    suspend fun deleteCardById(id: Long)

    @Query("UPDATE cards SET usageCount = usageCount + 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun incrementUsageCount(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM cards ORDER BY storeName ASC")
    fun getAllCardsSortedAZ(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards ORDER BY storeName DESC")
    fun getAllCardsSortedZA(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards ORDER BY usageCount DESC, storeName ASC")
    fun getAllCardsSortedByUsage(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards")
    suspend fun getAllCardsOnce(): List<CardEntity>

    @Query("SELECT * FROM cards WHERE syncId = :syncId LIMIT 1")
    suspend fun getCardBySyncId(syncId: String): CardEntity?

    @Query("UPDATE cards SET syncId = :syncId WHERE id = :id")
    suspend fun updateSyncId(id: Long, syncId: String)

    @Query("SELECT * FROM cards WHERE cardNumber = :cardNumber LIMIT 1")
    suspend fun getCardByCardNumber(cardNumber: String): CardEntity?

    @Query("SELECT COUNT(*) FROM cards WHERE isFavorite = 1")
    suspend fun getFavoriteCount(): Int

    @Query("DELETE FROM cards")
    suspend fun deleteAllCards()
}
