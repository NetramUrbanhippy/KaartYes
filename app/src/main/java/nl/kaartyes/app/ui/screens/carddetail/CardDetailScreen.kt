package nl.kaartyes.app.ui.screens.carddetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import nl.kaartyes.app.R
import nl.kaartyes.app.domain.model.BarcodeFormat
import nl.kaartyes.app.ui.components.BarcodeDisplay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    cardId: Long,
    onBack: () -> Unit,
    onEditCard: () -> Unit,
    onNotes: () -> Unit,
    onPhotos: () -> Unit,
    viewModel: CardDetailViewModel = hiltViewModel()
) {
    val card by viewModel.card.collectAsState()
    val showQr by viewModel.showQr.collectAsState()
    val showFavoriteLimitDialog by viewModel.showFavoriteLimitDialog.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(cardId) {
        viewModel.loadCard(cardId)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_card)) },
            text = { Text(stringResource(R.string.delete_card_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteCard(cardId, onBack)
                }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showFavoriteLimitDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissFavoriteLimitDialog() },
            title = { Text(stringResource(R.string.toggle_favorite)) },
            text = { Text(stringResource(R.string.favorite_limit_reached)) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissFavoriteLimitDialog() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back_button))
                    }
                },
                actions = {
                    val isFavorite = card?.isFavorite ?: false
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = stringResource(R.string.toggle_favorite),
                            tint = if (isFavorite) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        card?.let { c ->
            val tileColor = runCatching {
                Color(android.graphics.Color.parseColor(c.colorHex))
            }.getOrDefault(Color(0xFF1976D2))

            val isLightColor = run {
                val r = tileColor.red; val g = tileColor.green; val b = tileColor.blue
                (0.2126f * r + 0.7152f * g + 0.0722f * b) > 0.4f
            }
            val useLightText = c.textColorLight ?: !isLightColor
            val onTileColor = if (useLightText) Color.White else Color(0xFF1A1A1A)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(tileColor, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = c.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = onTileColor
                            )
                            TextButton(
                                onClick = onEditCard,
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = onTileColor.copy(alpha = 0.85f)
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.background(onTileColor.copy(alpha = 0.15f), RoundedCornerShape(50))
                            ) {
                                Text(stringResource(R.string.details), style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        val displayFormat = if (showQr) BarcodeFormat.QR_CODE else c.barcodeFormat
                        BarcodeDisplay(
                            cardNumber = c.cardNumber,
                            format = displayFormat,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (c.barcodeFormat == BarcodeFormat.QR_CODE || c.barcodeFormat == BarcodeFormat.CODE_128) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { viewModel.toggleDisplayFormat() }) {
                            Text(
                                text = if (showQr) stringResource(R.string.barcode) else stringResource(R.string.qr_code),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.manage),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Column {
                        ManageItem(
                            icon = { Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.onSurface) },
                            label = stringResource(R.string.edit_card),
                            onClick = onEditCard,
                            showDivider = true
                        )
                        ManageItem(
                            icon = { Icon(Icons.Default.StickyNote2, null, tint = MaterialTheme.colorScheme.onSurface) },
                            label = stringResource(R.string.notes),
                            onClick = onNotes,
                            showDivider = true
                        )
                        ManageItem(
                            icon = { Icon(Icons.Default.PhotoCamera, null, tint = MaterialTheme.colorScheme.onSurface) },
                            label = stringResource(R.string.photos),
                            onClick = onPhotos,
                            showDivider = true
                        )
                        ManageItem(
                            icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            label = stringResource(R.string.delete_card),
                            labelColor = MaterialTheme.colorScheme.error,
                            onClick = { showDeleteDialog = true },
                            showDivider = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        } ?: Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun ManageItem(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
    showDivider: Boolean,
    labelColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                icon()
                Text(text = label, style = MaterialTheme.typography.bodyLarge, color = labelColor)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
