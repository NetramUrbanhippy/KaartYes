package nl.kaartyes.app.ui.screens.editcard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import nl.kaartyes.app.R
import nl.kaartyes.app.ui.theme.TileColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCardScreen(
    cardId: Long,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: EditCardViewModel = hiltViewModel()
) {
    val card by viewModel.card.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    var cardNumber by remember { mutableStateOf("") }
    var storeName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#1976D2") }
    var textLight by remember { mutableStateOf(false) }

    LaunchedEffect(cardId) {
        viewModel.loadCard(cardId)
    }

    LaunchedEffect(card) {
        card?.let {
            cardNumber = it.cardNumber
            storeName = it.storeName
            selectedColor = it.colorHex
            textLight = it.textColorLight ?: autoTextLight(it.colorHex)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_card_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back_button))
                    }
                },
                actions = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.saveCard(cardNumber, storeName, selectedColor, textLight, onSaved)
                    },
                    enabled = !isSaving && cardNumber.isNotBlank() && storeName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.save),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.edit_card_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = storeName,
                onValueChange = { storeName = it },
                label = { Text(stringResource(R.string.store_name)) },
                trailingIcon = {
                    if (storeName.isNotBlank()) {
                        IconButton(onClick = { storeName = "" }) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = cardNumber,
                onValueChange = { cardNumber = it },
                label = { Text(stringResource(R.string.card_number)) },
                trailingIcon = {
                    if (cardNumber.isNotBlank()) {
                        IconButton(onClick = { cardNumber = "" }) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Text(
                text = stringResource(R.string.card_color),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(TileColors) { color ->
                    val hexColor = "#%06X".format(color.toArgb() and 0xFFFFFF)
                    val isSelected = hexColor.equals(selectedColor, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { selectedColor = hexColor }
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 2.dp,
                                color = if (isSelected) Color.White else Color.Black.copy(alpha = 0.15f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }

            Text(
                text = stringResource(R.string.card_text_color),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val previewBg = runCatching {
                    Color(android.graphics.Color.parseColor(selectedColor))
                }.getOrDefault(Color(0xFF1976D2))
                val previewText = buildPreviewInitials(storeName)
                val previewTextColor = if (textLight) Color.White else Color(0xFF1A1A1A)

                Box(
                    modifier = Modifier
                        .size(72.dp, 45.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(previewBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = previewText,
                        color = previewTextColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !textLight,
                        onClick = { textLight = false },
                        label = { Text(stringResource(R.string.text_dark)) }
                    )
                    FilterChip(
                        selected = textLight,
                        onClick = { textLight = true },
                        label = { Text(stringResource(R.string.text_light)) }
                    )
                }
            }
        }
    }
}

private fun autoTextLight(colorHex: String): Boolean {
    val color = runCatching { android.graphics.Color.parseColor(colorHex) }
        .getOrDefault(android.graphics.Color.parseColor("#1976D2"))
    val r = android.graphics.Color.red(color) / 255f
    val g = android.graphics.Color.green(color) / 255f
    val b = android.graphics.Color.blue(color) / 255f
    return (0.2126f * r + 0.7152f * g + 0.0722f * b) <= 0.4f
}

private fun buildPreviewInitials(name: String): String {
    val words = name.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
    return when {
        words.size >= 2 -> "${words[0].first()}${words[1].first()}".uppercase()
        words.isNotEmpty() -> words[0].take(2).uppercase()
        else -> "AB"
    }
}
