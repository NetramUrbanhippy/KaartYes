package nl.kaartyes.app.ui.screens.addcard

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import nl.kaartyes.app.R
import nl.kaartyes.app.domain.model.BarcodeFormat
import nl.kaartyes.app.ui.theme.Background
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(
    storeName: String,
    onCardAdded: (Long) -> Unit,
    onManualEntry: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddCardViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    var scannedValue by remember { mutableStateOf<String?>(null) }
    var scannedFormat by remember { mutableStateOf(BarcodeFormat.CODE_128) }
    val hasScanned = remember { AtomicBoolean(false) }

    // Scan animations
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val cornerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cornerAlpha"
    )
    val scanLineProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanLine"
    )

    val isDetected = scannedValue != null
    val cornerColor by animateColorAsState(
        targetValue = if (isDetected) Color(0xFF4CAF50) else Color.White.copy(alpha = cornerAlpha),
        animationSpec = tween(250),
        label = "cornerColor"
    )

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    LaunchedEffect(scannedValue) {
        scannedValue?.let { value ->
            delay(400)
            viewModel.addCard(
                cardNumber = value,
                storeName = storeName.ifBlank { value.take(20) },
                barcodeFormat = scannedFormat,
                onSuccess = onCardAdded
            )
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (storeName.isBlank()) stringResource(R.string.other_card)
                            else storeName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.scan_barcode),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back_button))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (cameraPermission.status.isGranted) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                        .background(Color.Black, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Camera preview
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val executor = Executors.newSingleThreadExecutor()
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build()
                                    .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                                val options = BarcodeScannerOptions.Builder()
                                    .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                                    .build()
                                val scanner = BarcodeScanning.getClient(options)

                                val imageAnalysis = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()

                                imageAnalysis.setAnalyzer(executor) { imageProxy ->
                                    val mediaImage = imageProxy.image
                                    if (mediaImage != null && !hasScanned.get()) {
                                        try {
                                            val image = InputImage.fromMediaImage(
                                                mediaImage,
                                                imageProxy.imageInfo.rotationDegrees
                                            )
                                            scanner.process(image)
                                                .addOnSuccessListener { barcodes ->
                                                    barcodes.firstOrNull()?.rawValue?.let { value ->
                                                        if (hasScanned.compareAndSet(false, true)) {
                                                            val format = when (barcodes.first().format) {
                                                                Barcode.FORMAT_QR_CODE -> BarcodeFormat.QR_CODE
                                                                Barcode.FORMAT_EAN_13 -> BarcodeFormat.EAN_13
                                                                Barcode.FORMAT_EAN_8 -> BarcodeFormat.EAN_8
                                                                Barcode.FORMAT_CODE_39 -> BarcodeFormat.CODE_39
                                                                Barcode.FORMAT_ITF -> BarcodeFormat.ITF
                                                                Barcode.FORMAT_PDF417 -> BarcodeFormat.PDF_417
                                                                Barcode.FORMAT_AZTEC -> BarcodeFormat.AZTEC
                                                                Barcode.FORMAT_DATA_MATRIX -> BarcodeFormat.DATA_MATRIX
                                                                else -> BarcodeFormat.CODE_128
                                                            }
                                                            scannedFormat = format
                                                            scannedValue = value
                                                        }
                                                    }
                                                }
                                                .addOnCompleteListener { imageProxy.close() }
                                        } catch (e: Exception) {
                                            imageProxy.close()
                                        }
                                    } else {
                                        imageProxy.close()
                                    }
                                }

                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageAnalysis
                                )
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Scan frame overlay
                    Box(
                        modifier = Modifier
                            .size(width = 260.dp, height = 140.dp)
                    ) {
                        // Animated scan line (only while not yet detected)
                        if (!isDetected) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val lineY = size.height * scanLineProgress
                                drawLine(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color(0xFF2196F3).copy(alpha = 0.8f),
                                            Color.Transparent
                                        ),
                                        startX = 0f,
                                        endX = size.width
                                    ),
                                    start = Offset(0f, lineY),
                                    end = Offset(size.width, lineY),
                                    strokeWidth = 2.dp.toPx()
                                )
                            }
                        }

                        ScanCorners(color = cornerColor)
                    }

                    // Status text at the bottom of the camera box
                    Text(
                        text = if (isDetected) stringResource(R.string.card_found)
                               else stringResource(R.string.point_camera_at_barcode),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDetected) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.8f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 20.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.camera_permission_required),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        TextButton(onClick = { cameraPermission.launchPermissionRequest() }) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }

            // Bottom options
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.onSurface)
                            Text(
                                text = stringResource(R.string.enter_manually),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        IconButton(onClick = onManualEntry) {
                            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanCorners(color: Color = Color.White) {
    val cornerLength = 24.dp
    val strokeWidth = 3.dp

    Box(modifier = Modifier.fillMaxSize()) {
        // Top-left
        Box(modifier = Modifier.align(Alignment.TopStart)) {
            Box(modifier = Modifier.size(cornerLength, strokeWidth).background(color))
            Box(modifier = Modifier.size(strokeWidth, cornerLength).background(color))
        }
        // Top-right
        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            Box(modifier = Modifier.size(cornerLength, strokeWidth).align(Alignment.TopEnd).background(color))
            Box(modifier = Modifier.size(strokeWidth, cornerLength).align(Alignment.TopEnd).background(color))
        }
        // Bottom-left
        Box(modifier = Modifier.align(Alignment.BottomStart)) {
            Box(modifier = Modifier.size(cornerLength, strokeWidth).align(Alignment.BottomStart).background(color))
            Box(modifier = Modifier.size(strokeWidth, cornerLength).align(Alignment.BottomStart).background(color))
        }
        // Bottom-right
        Box(modifier = Modifier.align(Alignment.BottomEnd)) {
            Box(modifier = Modifier.size(cornerLength, strokeWidth).align(Alignment.BottomEnd).background(color))
            Box(modifier = Modifier.size(strokeWidth, cornerLength).align(Alignment.BottomEnd).background(color))
        }
    }
}
