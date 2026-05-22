package nl.kaartyes.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.kaartyes.app.domain.model.Card

@Composable
fun CardTile(
    card: Card,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tileColor = runCatching { Color(android.graphics.Color.parseColor(card.colorHex)) }
        .getOrDefault(Color(0xFF1976D2))

    val isLightColor = tileColor.luminance() > 0.4f
    val useLightText = card.textColorLight ?: !isLightColor
    val textColor = if (useLightText) Color.White else Color(0xFF1A1A1A)
    val pillBgColor = if (useLightText) Color.White.copy(alpha = 0.22f) else Color.Black.copy(alpha = 0.12f)

    val bgLuminance = run {
        val bg = MaterialTheme.colorScheme.background
        0.2126f * bg.red + 0.7152f * bg.green + 0.0722f * bg.blue
    }
    val isDark = bgLuminance < 0.1f

    Box(
        modifier = modifier
            .aspectRatio(1.6f)
            .then(
                if (isDark) Modifier.border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                else Modifier
            )
            .clip(RoundedCornerShape(16.dp))
            .background(tileColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = card.initials,
                color = textColor,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            if (card.nickname.isNotBlank() || card.storeName.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .background(pillBgColor, RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = card.displayName,
                        color = textColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (card.isFavorite) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(5.dp)
                    .size(18.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.Black.copy(alpha = 0.55f),
                    modifier = Modifier.size(18.dp)
                )
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

private fun Color.luminance(): Float {
    return 0.2126f * red + 0.7152f * green + 0.0722f * blue
}
