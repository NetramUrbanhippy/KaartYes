package nl.kaartyes.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import nl.kaartyes.app.domain.model.BarcodeFormat
import nl.kaartyes.app.domain.model.Card

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val syncId: String = "",
    val cardNumber: String,
    val storeName: String,
    val nickname: String = "",
    val colorHex: String = "#1976D2",
    val textColorLight: Boolean? = null,
    val isFavorite: Boolean = false,
    val barcodeFormat: String = BarcodeFormat.CODE_128.name,
    val note: String = "",
    val frontImagePath: String? = null,
    val backImagePath: String? = null,
    val usageCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

fun CardEntity.toDomain() = Card(
    id = id,
    syncId = syncId,
    cardNumber = cardNumber,
    storeName = storeName,
    nickname = nickname,
    colorHex = colorHex,
    textColorLight = textColorLight,
    isFavorite = isFavorite,
    barcodeFormat = runCatching { BarcodeFormat.valueOf(barcodeFormat) }.getOrDefault(BarcodeFormat.CODE_128),
    note = note,
    frontImagePath = frontImagePath,
    backImagePath = backImagePath,
    usageCount = usageCount,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Card.toEntity() = CardEntity(
    id = id,
    syncId = syncId,
    cardNumber = cardNumber,
    storeName = storeName,
    nickname = nickname,
    colorHex = colorHex,
    textColorLight = textColorLight,
    isFavorite = isFavorite,
    barcodeFormat = barcodeFormat.name,
    note = note,
    frontImagePath = frontImagePath,
    backImagePath = backImagePath,
    usageCount = usageCount,
    createdAt = createdAt,
    updatedAt = updatedAt
)
