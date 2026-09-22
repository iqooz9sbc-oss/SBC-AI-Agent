package com.yourapp.features.headshot

import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourapp.core.credits.CreditViewModel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

/**
 * Entry point for the "Ultra-realistic Professional Headshot Generator" feature.
 *
 * @param creditViewModel the app's existing shared credit/role ViewModel
 * @param repository Gemini-backed repository (inject a fake in tests/previews)
 * @param onNavigateToPaywall called when the user needs to top up credits
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeadshotGeneratorScreen(
    creditViewModel: CreditViewModel,
    repository: HeadshotRepository,
    onNavigateToPaywall: () -> Unit,
    onBack: () -> Unit = {}
) {
    val viewModel: HeadshotViewModel = viewModel(
        factory = HeadshotViewModel.provideFactory(creditViewModel, repository)
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedStyle by viewModel.selectedStyle.collectAsStateWithLifecycle()
    val creditBalance by viewModel.creditBalance.collectAsStateWithLifecycle()
    val userRole by creditViewModel.userRole.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val bitmap = loadBitmapFromUri(context, uri)
            if (bitmap != null) {
                viewModel.onImageSelected(bitmap)
            } else {
                viewModel.onImageSelectionFailed("Couldn't read that image. Please try another.")
            }
        }
    }

    // One-off events: paywall navigation, snackbars, save confirmations.
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HeadshotEvent.ShowPaywall -> onNavigateToPaywall()
                is HeadshotEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is HeadshotEvent.ImageSaved -> snackbarHostState.showSnackbar("Saved to your gallery")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Professional Headshot") },
                actions = {
                    CreditsBadge(
                        isAdmin = userRole == "ADMIN",
                        credits = creditBalance
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StyleSelector(
                selected = selectedStyle,
                onSelect = viewModel::onStyleSelected,
                enabled = uiState !is HeadshotUiState.Loading
            )

            ImagePreviewSection(
                uiState = uiState,
                onPickImage = { imagePickerLauncher.launch("image/*") }
            )

            ActionButtons(
                uiState = uiState,
                onGenerate = viewModel::generateHeadshot,
                onReset = viewModel::reset,
                onSave = { bitmap ->
                    scope.launch {
                        val uri = saveBitmapToGallery(context, bitmap)
                        if (uri != null) {
                            viewModel.onImageSaved(uri)
                        } else {
                            snackbarHostState.showSnackbar("Couldn't save image.")
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun CreditsBadge(isAdmin: Boolean, credits: Int) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (isAdmin) MaterialTheme.colorScheme.tertiaryContainer
        else MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = if (isAdmin) "Admin · Unlimited" else "$credits credits",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StyleSelector(
    selected: HeadshotStyle,
    onSelect: (HeadshotStyle) -> Unit,
    enabled: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Style",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HeadshotStyle.entries.forEach { style ->
                FilterChip(
                    selected = style == selected,
                    onClick = { onSelect(style) },
                    label = { Text(style.displayName) },
                    enabled = enabled
                )
            }
        }
    }
}

@Composable
private fun ImagePreviewSection(
    uiState: HeadshotUiState,
    onPickImage: () -> Unit
) {
    when (uiState) {
        is HeadshotUiState.Idle -> UploadPlaceholder(onPickImage)

        is HeadshotUiState.ImageSelected -> StackedPreview(
            input = uiState.inputImage,
            output = null,
            isLoading = false,
            onPickImage = onPickImage
        )

        is HeadshotUiState.Loading -> StackedPreview(
            input = uiState.inputImage,
            output = null,
            isLoading = true,
            onPickImage = onPickImage
        )

        is HeadshotUiState.Success -> StackedPreview(
            input = uiState.inputImage,
            output = uiState.outputImage,
            isLoading = false,
            onPickImage = onPickImage
        )

        is HeadshotUiState.Error -> Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (uiState.inputImage != null) {
                StackedPreview(
                    input = uiState.inputImage,
                    output = null,
                    isLoading = false,
                    onPickImage = onPickImage
                )
            } else {
                UploadPlaceholder(onPickImage)
            }
            Text(
                text = uiState.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun UploadPlaceholder(onPickImage: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onPickImage
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Tap to upload a photo", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/**
 * Stacked (vertical) preview of input → output. Swap the Column for a Row on
 * wide/tablet layouts using BoxWithConstraints if you want a true side-by-side
 * layout on larger screens.
 */
@Composable
private fun StackedPreview(
    input: Bitmap,
    output: Bitmap?,
    isLoading: Boolean,
    onPickImage: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        PreviewCard(
            title = "Original",
            bitmap = input,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .then(if (!isLoading) Modifier.clickable { onPickImage() } else Modifier)
        )

        PreviewCard(
            title = "Professional Headshot",
            bitmap = output,
            isLoading = isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )
    }
}

@Composable
private fun PreviewCard(
    title: String,
    bitmap: Bitmap?,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Surface(
            modifier = modifier.clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                if (isLoading) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Generating your headshot…",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else if (bitmap == null) {
                    Text(
                        "Nothing yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButtons(
    uiState: HeadshotUiState,
    onGenerate: () -> Unit,
    onReset: () -> Unit,
    onSave: (Bitmap) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        when (uiState) {
            is HeadshotUiState.ImageSelected, is HeadshotUiState.Error -> {
                Button(
                    onClick = onGenerate,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Generate Headshot")
                }
            }

            is HeadshotUiState.Loading -> {
                Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generating…")
                }
            }

            is HeadshotUiState.Success -> {
                Button(
                    onClick = { onSave(uiState.outputImage) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save to Gallery")
                }
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Another")
                }
            }

            else -> Unit
        }
    }
}

// --- Bitmap I/O helpers ---

private suspend fun loadBitmapFromUri(context: android.content.Context, uri: Uri): Bitmap? =
    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                android.graphics.BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            null
        }
    }

/**
 * Saves the generated headshot to the device gallery via MediaStore (no storage
 * permission needed on API 29+; on older APIs ensure WRITE_EXTERNAL_STORAGE is
 * declared/granted before calling this).
 */
private suspend fun saveBitmapToGallery(
    context: android.content.Context,
    bitmap: Bitmap
): Uri? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    try {
        val filename = "headshot_${System.currentTimeMillis()}.jpg"
        val resolver = context.contentResolver

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ProfessionalHeadshots")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val uri = resolver.insert(collection, contentValues) ?: return@withContext null

        resolver.openOutputStream(uri)?.use { out ->
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
            out.write(stream.toByteArray())
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }

        uri
    } catch (e: Exception) {
        null
    }
}
