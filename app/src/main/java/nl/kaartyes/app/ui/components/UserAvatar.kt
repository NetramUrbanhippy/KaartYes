package nl.kaartyes.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseUser

/**
 * Rond profielbolletje: Google-profielfoto als die er is, anders de eerste
 * letter van de naam (of het e-mailadres).
 */
@Composable
fun UserAvatar(user: FirebaseUser?, size: Dp = 32.dp) {
    val photoUrl = user?.photoUrl
    var imageFailed by remember(photoUrl) { mutableStateOf(false) }
    val initial = (user?.displayName?.takeIf { it.isNotBlank() } ?: user?.email ?: "?")
        .trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    if (photoUrl != null && !imageFailed) {
        AsyncImage(
            model = photoUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            onError = { imageFailed = true },
            modifier = Modifier.size(size).clip(CircleShape)
        )
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = initial,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = (size.value * 0.45f).sp
            )
        }
    }
}
