package com.example.rl_v1.ui.theme.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

/** Red used for destructive actions, matching "Delete Profile" on the Profile screen. */
private val DestructiveRed = Color(0xFFC4532D)

/**
 * Lets the user change or remove their profile photo.
 *
 * Frontend only: picking, taking and uploading a photo are stubbed with TODOs. The screen keeps
 * track of a *pending* change (new photo or removal) and only reports it through [onSave].
 *
 * @param displayName used for the initials shown when there is no photo.
 * @param currentPhoto the saved photo, or null if the user has none.
 * @param onBack leave without saving (back arrow and Cancel).
 * @param onSave called with the photo to keep, or null to remove it.
 */
@Composable
fun ProfilePictureScreen(
    displayName: String = "Sample Camper",
    currentPhoto: Painter? = null,
    onBack: () -> Unit = {},
    onSave: (photo: Painter?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // TODO: replace Painter with a Uri (or download URL) once picking/upload is wired up,
    //  so the pending photo survives rotation via rememberSaveable.
    var pendingPhoto by remember(currentPhoto) { mutableStateOf(currentPhoto) }
    var hasChanges by rememberSaveable { mutableStateOf(false) }

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
            ProfileAvatar(
                displayName = displayName,
                photo = pendingPhoto,
                size = 176.dp
            )
            // Camera badge, a shortcut to taking a photo.
            Surface(
                onClick = {
                    // TODO: launch the camera (ActivityResultContracts.TakePicture) and set pendingPhoto.
                    hasChanges = true
                },
                shape = CircleShape,
                color = DarkGreen,
                border = BorderStroke(4.dp, Color.White),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = "Take a photo",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
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
                    onClick = {
                        // TODO: launch the photo picker (ActivityResultContracts.PickVisualMedia)
                        //  and set pendingPhoto to the chosen image.
                        hasChanges = true
                    }
                )
                PhotoOptionRow(
                    icon = Icons.Filled.PhotoCamera,
                    title = "Take a photo",
                    subtitle = "Use your camera",
                    onClick = {
                        // TODO: launch the camera (ActivityResultContracts.TakePicture) and set pendingPhoto.
                        hasChanges = true
                    }
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        PrimaryClickableText(
            text = "Remove photo",
            textColor = if (pendingPhoto != null) DestructiveRed else Color.Gray,
            enabled = pendingPhoto != null,
            onClick = {
                pendingPhoto = null
                hasChanges = true
            }
        )

        Spacer(Modifier.height(32.dp))
        Text(
            text = "JPG or PNG, square photos look best.",
            style = MaterialTheme.typography.bodySmall,
            color = GreyGreen
        )
        Spacer(Modifier.height(12.dp))

        LargeButtonPrimary(
            text = "Save photo",
            enabled = hasChanges,
            onClick = {
                // TODO: upload pendingPhoto (e.g. Firebase Storage) and update the user's profile.
                onSave(pendingPhoto)
            }
        )
        Spacer(Modifier.height(12.dp))
        LargeButtonSecondary(text = "Cancel", onClick = onBack)
    }
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

/** One tappable row in the photo options card: icon, title and a short description. */
@Composable
private fun PhotoOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
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
        ProfilePictureScreen(currentPhoto = ColorPainter(GreyGreen))
    }
}
