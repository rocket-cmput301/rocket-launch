package com.example.rl_v1.ui.theme.screens.profile

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil3.compose.rememberAsyncImagePainter
import com.example.rl_v1.storage.ImageCropper
import com.example.rl_v1.ui.theme.components.LargeButtonPrimary
import com.example.rl_v1.ui.theme.components.LargeButtonSecondary
import com.example.rl_v1.ui.theme.components.PrimaryClickableText
import com.example.rl_v1.ui.theme.components.ScreenHeader
import com.example.rl_v1.ui.theme.theme.Cream
import com.example.rl_v1.ui.theme.theme.DarkGreen
import com.example.rl_v1.ui.theme.theme.Forest
import com.example.rl_v1.ui.theme.theme.GreyGreen
import com.example.rl_v1.ui.theme.theme.LightGreen
import com.example.rl_v1.ui.theme.theme.Rlv1Theme
import java.io.File
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Red used for destructive actions, matching "Delete Profile" on the Profile screen. */
private val DestructiveRed = Color(0xFFC4532D)

/**
 * Lets the user change or remove their profile photo.
 *
 * Keeps track of a *pending* change (new photo URL/URI or removal) and reports it via [onSave].
 * The caller uploads a newly picked local image to Firebase Storage and saves the resulting
 * download URL; while that runs it passes [isSaving], and on failure an [errorMessage].
 *
 * A newly picked or taken photo is shown inside the circle where the user can drag to move it and
 * pinch to zoom. On save, the visible square is cropped to a 512x512 JPEG and passed to
 * [onSaveAdjusted]; if the image can't be decoded for adjusting, the raw URI goes to [onSave].
 *
 * @param displayName used for the initials shown when there is no photo.
 * @param currentPhotoUrl the saved photo URL/URI string, or null if the user has none.
 * @param defaultPhoto optional fallback painter when there is no active photo URL.
 * @param onBack leave without saving (back arrow and Cancel).
 * @param onSave called with the photo URL/URI string to keep, or null to remove it.
 * @param isSaving true while the photo is being uploaded/saved; shows a spinner and disables actions.
 * @param errorMessage message shown when saving the photo failed, or null for none.
 * @param onSaveAdjusted called with the cropped JPEG bytes of a newly picked, adjusted photo.
 */
@Composable
fun ProfilePictureScreen(
    displayName: String = "Sample Camper",
    currentPhotoUrl: String? = null,
    defaultPhoto: Painter? = null,
    onBack: () -> Unit = {},
    onSave: (photoUrl: String?) -> Unit = {},
    isSaving: Boolean = false,
    errorMessage: String? = null,
    onSaveAdjusted: (jpegBytes: ByteArray) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Framing of a newly picked photo, see ImageCropper for the units.
    var adjustZoom by rememberSaveable { mutableStateOf(1f) }
    var adjustOffsetX by rememberSaveable { mutableStateOf(0f) }
    var adjustOffsetY by rememberSaveable { mutableStateOf(0f) }
    var adjustBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isPreparing by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    val busy = isSaving || isPreparing
    val resetFraming = {
        adjustZoom = 1f
        adjustOffsetX = 0f
        adjustOffsetY = 0f
        localError = null
    }

    var seededPhotoUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingPhotoUrl by rememberSaveable { mutableStateOf(currentPhotoUrl) }
    var hasChanges by rememberSaveable { mutableStateOf(false) }
    var tempCameraUriString by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(currentPhotoUrl) {
        if (seededPhotoUrl == null || (!hasChanges && currentPhotoUrl != seededPhotoUrl)) {
            pendingPhotoUrl = currentPhotoUrl
            seededPhotoUrl = currentPhotoUrl
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            resetFraming()
            pendingPhotoUrl = uri.toString()
            hasChanges = true
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { taken: Boolean ->
        val capturedUri = tempCameraUriString
        if (taken && !capturedUri.isNullOrBlank()) {
            resetFraming()
            pendingPhotoUrl = capturedUri
            hasChanges = true
        }
    }

    val launchCamera: () -> Unit = {
        val uri = createTempCameraUri(context)
        tempCameraUriString = uri.toString()
        cameraLauncher.launch(uri)
    }

    val activePainter: Painter? = if (!pendingPhotoUrl.isNullOrBlank()) {
        rememberAsyncImagePainter(
            model = pendingPhotoUrl,
            onError = { error ->
                Log.e(
                    "ProfilePictureScreen",
                    "Failed to load profile photo: $pendingPhotoUrl",
                    error.result.throwable
                )
            }
        )
    } else {
        defaultPhoto
    }

    // Only a newly picked local image (not an already saved remote URL) can be adjusted.
    val localPhotoUri = pendingPhotoUrl?.takeIf {
        hasChanges && it.isNotBlank() && !it.startsWith("http://") && !it.startsWith("https://")
    }
    // True when the picked photo couldn't be decoded; it is then uploaded as is, unadjusted.
    var decodeFailed by remember { mutableStateOf(false) }
    LaunchedEffect(localPhotoUri) {
        adjustBitmap = null
        decodeFailed = false
        if (localPhotoUri != null) {
            adjustBitmap = try {
                ImageCropper.decodeUpright(context, Uri.parse(localPhotoUri))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("ProfilePictureScreen", "Could not decode photo for adjusting", e)
                decodeFailed = true
                null
            }
        }
    }
    val bitmapToAdjust = adjustBitmap.takeIf { localPhotoUri != null }

    val onSaveClick: () -> Unit = {
        val bitmap = bitmapToAdjust
        if (bitmap != null) {
            coroutineScope.launch {
                isPreparing = true
                localError = null
                try {
                    val bytes = ImageCropper.cropToJpeg(
                        source = bitmap,
                        zoom = adjustZoom,
                        offsetX = adjustOffsetX,
                        offsetY = adjustOffsetY
                    )
                    onSaveAdjusted(bytes)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e("ProfilePictureScreen", "Could not crop photo", e)
                    localError = "Couldn't prepare your photo. Please try another one."
                } finally {
                    isPreparing = false
                }
            }
        } else {
            onSave(pendingPhotoUrl?.takeIf { it.isNotBlank() })
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Forest
                )
            }
            Spacer(Modifier.width(4.dp))
            ScreenHeader("Profile photo")
        }

        Spacer(Modifier.height(32.dp))

        Box(contentAlignment = Alignment.BottomEnd) {
            if (bitmapToAdjust != null) {
                AdjustablePhotoAvatar(
                    bitmap = bitmapToAdjust,
                    zoom = adjustZoom,
                    offsetX = adjustOffsetX,
                    offsetY = adjustOffsetY,
                    enabled = !busy,
                    onFramingChange = { zoom, offsetX, offsetY ->
                        adjustZoom = zoom
                        adjustOffsetX = offsetX
                        adjustOffsetY = offsetY
                    },
                    size = 176.dp
                )
            } else {
                ProfileAvatar(
                    displayName = displayName,
                    photo = activePainter,
                    size = 176.dp
                )
            }
            // Camera badge, a shortcut to taking a photo.
            Surface(
                onClick = {
                    galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                enabled = !busy,
                shape = CircleShape,
                color = DarkGreen,
                border = BorderStroke(4.dp, Color.White),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = "Pick a photo",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        if (bitmapToAdjust != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Drag and pinch to adjust",
                style = MaterialTheme.typography.bodySmall,
                color = GreyGreen
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = displayName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = Forest
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Your photo is shown to hosts and other campers on your reviews and reservations.",
            style = MaterialTheme.typography.bodyMedium,
            color = GreyGreen,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(32.dp))

        // Photo source options
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Cream,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PhotoOptionRow(
                    icon = Icons.Outlined.PhotoLibrary,
                    title = "Choose from gallery",
                    subtitle = "Pick a photo saved on your device",
                    enabled = !busy,
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
                /*PhotoOptionRow(
                    icon = Icons.Filled.PhotoCamera,
                    title = "Take a photo",
                    subtitle = "Use your camera",
                    enabled = !busy,
                    onClick = launchCamera
                )*/
            }
        }

        Spacer(Modifier.height(12.dp))
        PrimaryClickableText(
            text = "Remove photo",
            textColor = if (!pendingPhotoUrl.isNullOrBlank() && !busy) DestructiveRed else Color.Gray,
            enabled = !pendingPhotoUrl.isNullOrBlank() && !busy,
            onClick = {
                pendingPhotoUrl = null
                hasChanges = true
                localError = null
            }
        )

        Spacer(Modifier.height(32.dp))
        Text(
            text = "JPG or PNG, square photos look best.",
            style = MaterialTheme.typography.bodySmall,
            color = GreyGreen
        )
        Spacer(Modifier.height(12.dp))

        if (busy) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                CircularProgressIndicator(
                    color = DarkGreen,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Uploading photo...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GreyGreen
                )
            }
        }
        val shownError = localError ?: errorMessage
        if (!shownError.isNullOrBlank() && !busy) {
            Text(
                text = shownError,
                style = MaterialTheme.typography.bodyMedium,
                color = DestructiveRed,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        LargeButtonPrimary(
            text = if (busy) "Saving..." else "Save photo",
            // Wait for a newly picked photo to finish decoding so its framing is what gets saved.
            enabled = hasChanges && !busy && (localPhotoUri == null || adjustBitmap != null || decodeFailed),
            onClick = onSaveClick
        )
        Spacer(Modifier.height(12.dp))
        LargeButtonSecondary(text = "Cancel", onClick = onBack)
    }
}

private fun createTempCameraUri(context: Context): Uri {
    val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
    val imageFile = File.createTempFile("profile_photo_", ".jpg", imagesDir)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}

/**
 * Round profile picture. Shows [photo] cropped to a circle, or the user's initials on a light
 * green background when there is no photo.
 */
@Composable
fun ProfileAvatar(
    displayName: String,
    photo: Painter?,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(LightGreen)
            .border(BorderStroke(3.dp, DarkGreen), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (photo != null) {
            Image(
                painter = photo,
                contentDescription = "Profile photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = initialsOf(displayName),
                color = DarkGreen,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.36f).sp
            )
        }
    }
}

/**
 * Circular preview, styled like [ProfileAvatar], in which [bitmap] can be dragged to pan and
 * pinched to zoom. The image always covers the circle: zoom is clamped to
 * 1..[ImageCropper.MAX_ZOOM] (1 = cover fit) and the offset to keep the edges outside the circle.
 *
 * @param zoom current zoom relative to a cover fit.
 * @param offsetX horizontal image offset as a fraction of the circle diameter.
 * @param offsetY vertical image offset as a fraction of the circle diameter.
 * @param onFramingChange called with the new, clamped (zoom, offsetX, offsetY) on each gesture.
 */
@Composable
private fun AdjustablePhotoAvatar(
    bitmap: Bitmap,
    zoom: Float,
    offsetX: Float,
    offsetY: Float,
    enabled: Boolean,
    onFramingChange: (zoom: Float, offsetX: Float, offsetY: Float) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp
) {
    val image = remember(bitmap) { bitmap.asImageBitmap() }
    // The gesture handler outlives recompositions and gets several events between them, so it
    // keeps the latest framing (zoom, offsetX, offsetY) in a plain, non-state holder.
    val latest = remember { FloatArray(3) }
    latest[0] = zoom
    latest[1] = offsetX
    latest[2] = offsetY

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(LightGreen)
            .border(BorderStroke(3.dp, DarkGreen), CircleShape)
            .pointerInput(bitmap, enabled) {
                if (!enabled) return@pointerInput
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    val diameter = this.size.width.toFloat().coerceAtLeast(1f)
                    val newZoom = (latest[0] * gestureZoom).coerceIn(1f, ImageCropper.MAX_ZOOM)
                    val (maxX, maxY) = ImageCropper.maxOffsets(bitmap.width, bitmap.height, newZoom)
                    val newOffsetX = (latest[1] + pan.x / diameter).coerceIn(-maxX, maxX)
                    val newOffsetY = (latest[2] + pan.y / diameter).coerceIn(-maxY, maxY)
                    latest[0] = newZoom
                    latest[1] = newOffsetX
                    latest[2] = newOffsetY
                    onFramingChange(newZoom, newOffsetX, newOffsetY)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val diameter = this.size.width
            // Same maths as ImageCropper.cropSquare, so what is visible is what gets uploaded.
            val pixelsPerSourcePixel = diameter / min(bitmap.width, bitmap.height) * zoom
            val drawWidth = bitmap.width * pixelsPerSourcePixel
            val drawHeight = bitmap.height * pixelsPerSourcePixel
            val left = diameter / 2f + offsetX * diameter - drawWidth / 2f
            val top = diameter / 2f + offsetY * diameter - drawHeight / 2f
            drawImage(
                image = image,
                dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
                dstSize = IntSize(drawWidth.roundToInt(), drawHeight.roundToInt()),
                filterQuality = FilterQuality.Medium
            )
        }
    }
}

/** One tappable row in the photo options card: icon, title and a short description. */
@Composable
private fun PhotoOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(LightGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = DarkGreen)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Forest
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GreyGreen
                )
            }
        }
    }
}

/** "Sample Camper" -> "SC". Falls back to "?" for a blank name. */
private fun initialsOf(name: String): String =
    name.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "No photo")
@Composable
private fun ProfilePictureScreenPreview() {
    Rlv1Theme(dynamicColor = false) {
        ProfilePictureScreen()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "With photo")
@Composable
private fun ProfilePictureScreenWithPhotoPreview() {
    Rlv1Theme(dynamicColor = false) {
        // Stand-in for a real photo in the preview.
        ProfilePictureScreen(defaultPhoto = ColorPainter(GreyGreen))
    }
}
