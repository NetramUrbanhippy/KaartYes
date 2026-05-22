package nl.kaartyes.app.ui.screens.photos

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import nl.kaartyes.app.R
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotosScreen(
    cardId: Long,
    onBack: () -> Unit,
    viewModel: PhotosViewModel = hiltViewModel()
) {
    val card by viewModel.card.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(cardId) { viewModel.loadCard(cardId) }

    var showFrontSourceDialog by remember { mutableStateOf(false) }
    var showBackSourceDialog by remember { mutableStateOf(false) }
    var frontCameraUri by remember { mutableStateOf<Uri?>(null) }
    var backCameraUri by remember { mutableStateOf<Uri?>(null) }

    fun createCameraUri(): Uri {
        val dir = File(context.cacheDir, "camera_photos").also { it.mkdirs() }
        val file = File(dir, "temp_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    val frontCameraLauncher = rememberLauncherForActivityResult(TakePicture()) { success ->
        if (success) frontCameraUri?.let { viewModel.saveFrontPhoto(cardId, it) }
    }
    val backCameraLauncher = rememberLauncherForActivityResult(TakePicture()) { success ->
        if (success) backCameraUri?.let { viewModel.saveBackPhoto(cardId, it) }
    }
    val frontGalleryLauncher = rememberLauncherForActivityResult(GetContent()) { uri ->
        uri?.let { viewModel.saveFrontPhoto(cardId, it) }
    }
    val backGalleryLauncher = rememberLauncherForActivityResult(GetContent()) { uri ->
        uri?.let { viewModel.saveBackPhoto(cardId, it) }
    }

    if (showFrontSourceDialog) {
        PhotoSourceDialog(
            onDismiss = { showFrontSourceDialog = false },
            onCamera = {
                showFrontSourceDialog = false
                val uri = createCameraUri()
                frontCameraUri = uri
                frontCameraLauncher.launch(uri)
            },
            onGallery = {
                showFrontSourceDialog = false
                frontGalleryLauncher.launch("image/*")
            }
        )
    }

    if (showBackSourceDialog) {
        PhotoSourceDialog(
            onDismiss = { showBackSourceDialog = false },
            onCamera = {
                showBackSourceDialog = false
                val uri = createCameraUri()
                backCameraUri = uri
                backCameraLauncher.launch(uri)
            },
            onGallery = {
                showBackSourceDialog = false
                backGalleryLauncher.launch("image/*")
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.card_photos)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back_button))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.card_photos),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PhotoSlot(
                    label = stringResource(R.string.front),
                    imagePath = card?.frontImagePath,
                    onAdd = { showFrontSourceDialog = true },
                    onRemove = { viewModel.removeFrontPhoto(cardId) },
                    modifier = Modifier.weight(1f)
                )
                PhotoSlot(
                    label = stringResource(R.string.back),
                    imagePath = card?.backImagePath,
                    onAdd = { showBackSourceDialog = true },
                    onRemove = { viewModel.removeBackPhoto(cardId) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PhotoSourceDialog(
    onDismiss: () -> Unit,
    onCamera: () -> Unit,
    onGallery: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_photo)) },
        confirmButton = {
            TextButton(onClick = onCamera) {
                Text(stringResource(R.string.take_photo))
            }
        },
        dismissButton = {
            TextButton(onClick = onGallery) {
                Text(stringResource(R.string.choose_from_gallery))
            }
        }
    )
}

@Composable
private fun PhotoSlot(
    label: String,
    imagePath: String?,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.58f)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(12.dp)
                )
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onAdd),
            contentAlignment = Alignment.Center
        ) {
            if (imagePath != null && File(imagePath).exists()) {
                AsyncImage(
                    model = File(imagePath),
                    contentDescription = label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Remove button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.remove_photo),
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.AddPhotoAlternate,
                        contentDescription = stringResource(R.string.add_photo),
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = stringResource(R.string.add_photo),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}
