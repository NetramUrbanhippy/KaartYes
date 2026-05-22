package nl.kaartyes.app.ui.components

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.kaartyes.app.domain.model.BarcodeFormat as AppBarcodeFormat

@Composable
fun BarcodeDisplay(
    cardNumber: String,
    format: AppBarcodeFormat,
    modifier: Modifier = Modifier
) {
    var bitmap by remember(cardNumber, format) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(cardNumber, format) {
        bitmap = withContext(Dispatchers.Default) {
            generateBarcode(cardNumber, format)
        }
    }

    Column(
        modifier = modifier
            .background(androidx.compose.ui.graphics.Color.White, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        bitmap?.let { bmp ->
            Image(
                painter = BitmapPainter(bmp.asImageBitmap()),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (format == AppBarcodeFormat.QR_CODE) 180.dp else 100.dp),
                contentScale = ContentScale.Fit
            )
        } ?: Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(androidx.compose.ui.graphics.Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = formatCardNumber(cardNumber),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

private fun generateBarcode(content: String, format: AppBarcodeFormat): Bitmap? {
    if (content.isBlank()) return null
    return try {
        val zxingFormat = when (format) {
            AppBarcodeFormat.QR_CODE -> BarcodeFormat.QR_CODE
            AppBarcodeFormat.EAN_13 -> BarcodeFormat.EAN_13
            AppBarcodeFormat.EAN_8 -> BarcodeFormat.EAN_8
            AppBarcodeFormat.CODE_39 -> BarcodeFormat.CODE_39
            AppBarcodeFormat.ITF -> BarcodeFormat.ITF
            AppBarcodeFormat.PDF_417 -> BarcodeFormat.PDF_417
            AppBarcodeFormat.AZTEC -> BarcodeFormat.AZTEC
            AppBarcodeFormat.DATA_MATRIX -> BarcodeFormat.DATA_MATRIX
            else -> BarcodeFormat.CODE_128
        }

        val hints = mapOf(EncodeHintType.MARGIN to 1)
        val (width, height) = if (format == AppBarcodeFormat.QR_CODE) 512 to 512 else 900 to 300

        val bitMatrix = MultiFormatWriter().encode(content, zxingFormat, width, height, hints)
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        bmp
    } catch (e: WriterException) {
        null
    } catch (e: IllegalArgumentException) {
        null
    }
}

private fun formatCardNumber(number: String): String {
    if (number.length <= 4) return number
    return number.chunked(4).joinToString(" ")
}
