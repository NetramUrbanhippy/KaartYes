package nl.kaartyes.app.domain.model

data class Card(
    val id: Long = 0,
    val syncId: String = "",
    val cardNumber: String,
    val storeName: String,
    val nickname: String = "",
    val colorHex: String = "#1976D2",
    val textColorLight: Boolean? = null,
    val isFavorite: Boolean = false,
    val barcodeFormat: BarcodeFormat = BarcodeFormat.CODE_128,
    val note: String = "",
    val frontImagePath: String? = null,
    val backImagePath: String? = null,
    val usageCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val displayName: String get() = nickname.ifBlank { storeName }

    val initials: String get() {
        val words = displayName.trim().split("\\s+".toRegex())
        return when {
            words.size >= 2 -> "${words[0].first()}${words[1].first()}".uppercase()
            words.isNotEmpty() -> words[0].take(2).uppercase()
            else -> "?"
        }
    }
}

enum class BarcodeFormat {
    CODE_128,
    QR_CODE,
    EAN_13,
    EAN_8,
    CODE_39,
    ITF,
    PDF_417,
    AZTEC,
    DATA_MATRIX
}
