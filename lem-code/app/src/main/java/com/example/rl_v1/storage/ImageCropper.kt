package com.example.rl_v1.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Helpers for preparing a picked profile photo for upload: decoding it upright and downsampled,
 * and cropping the square the user framed in the circular preview.
 *
 * The framing is described independently of screen size:
 * - `zoom` is relative to a cover fit (1 = the image's shorter side exactly fills the circle).
 * - `offsetX`/`offsetY` move the image's centre away from the circle's centre, as a fraction of
 *   the circle's diameter (positive = image moved right/down).
 */
object ImageCropper {

    /** Largest zoom allowed in the adjust preview, relative to a cover fit. */
    const val MAX_ZOOM = 4f

    /** Longest side, in pixels, that picked images are decoded to. */
    private const val MAX_DECODE_SIZE = 1536

    /** Side length, in pixels, of the uploaded square picture. */
    private const val OUTPUT_SIZE = 512

    /** JPEG quality (0-100) of the uploaded picture. */
    private const val JPEG_QUALITY = 85

    /**
     * Decodes [uri] into an upright (EXIF rotation applied), software [Bitmap] whose longest side
     * is at most [maxSize] pixels. Runs on [Dispatchers.IO].
     */
    suspend fun decodeUpright(
        context: Context,
        uri: Uri,
        maxSize: Int = MAX_DECODE_SIZE
    ): Bitmap = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // ImageDecoder applies the EXIF orientation itself.
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val longest = max(info.size.width, info.size.height)
                if (longest > maxSize) {
                    val ratio = maxSize.toFloat() / longest
                    decoder.setTargetSize(
                        (info.size.width * ratio).roundToInt().coerceAtLeast(1),
                        (info.size.height * ratio).roundToInt().coerceAtLeast(1)
                    )
                }
            }
        } else {
            decodeWithBitmapFactory(context, uri, maxSize)
        }
    }

    /** API 26-27 fallback: [BitmapFactory] with power-of-two downsampling plus manual EXIF rotation. */
    private fun decodeWithBitmapFactory(context: Context, uri: Uri, maxSize: Int): Bitmap {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Could not read the selected image." }

        var sampleSize = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sampleSize * 2) >= maxSize) sampleSize *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decoded = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: error("Could not decode the selected image.")

        val orientation = resolver.openInputStream(uri)?.use {
            ExifInterface(it).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        } ?: ExifInterface.ORIENTATION_NORMAL
        val matrix = Matrix().apply {
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> postScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> { postRotate(90f); postScale(-1f, 1f) }
                ExifInterface.ORIENTATION_TRANSVERSE -> { postRotate(270f); postScale(-1f, 1f) }
            }
        }
        if (matrix.isIdentity) return decoded
        val upright = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
        if (upright !== decoded) decoded.recycle()
        return upright
    }

    /**
     * Largest allowed |offsetX| and |offsetY| (fractions of the circle diameter) for an image of
     * [width] x [height] at [zoom], so that the image always covers the whole circle.
     */
    fun maxOffsets(width: Int, height: Int, zoom: Float): Pair<Float, Float> {
        val shorter = min(width, height).toFloat()
        val maxX = ((width / shorter) * zoom - 1f) / 2f
        val maxY = ((height / shorter) * zoom - 1f) / 2f
        return maxX.coerceAtLeast(0f) to maxY.coerceAtLeast(0f)
    }

    /**
     * Square region, in source pixels, of a [width] x [height] image that is visible in the circle
     * for the given framing. Always lies inside the image.
     */
    fun cropRegion(
        width: Int,
        height: Int,
        zoom: Float,
        offsetX: Float,
        offsetY: Float
    ): CropRegion {
        val shorter = min(width, height).toFloat()
        val side = (shorter / zoom).coerceIn(1f, shorter)
        val centerX = width / 2f - offsetX * shorter / zoom
        val centerY = height / 2f - offsetY * shorter / zoom
        val left = (centerX - side / 2f).coerceIn(0f, width - side)
        val top = (centerY - side / 2f).coerceIn(0f, height - side)
        return CropRegion(
            left = left.roundToInt(),
            top = top.roundToInt(),
            right = (left + side).roundToInt().coerceAtMost(width),
            bottom = (top + side).roundToInt().coerceAtMost(height)
        )
    }

    /** A rectangle in source pixels; [right] and [bottom] are exclusive, like [Rect]. */
    data class CropRegion(val left: Int, val top: Int, val right: Int, val bottom: Int) {
        val width: Int get() = right - left
        val height: Int get() = bottom - top
    }

    /**
     * Crops the square of [source] that is visible in the circle for the given framing and scales
     * it to [outputSize] x [outputSize].
     */
    fun cropSquare(
        source: Bitmap,
        zoom: Float,
        offsetX: Float,
        offsetY: Float,
        outputSize: Int = OUTPUT_SIZE
    ): Bitmap {
        val region = cropRegion(source.width, source.height, zoom, offsetX, offsetY)
        val srcRect = Rect(region.left, region.top, region.right, region.bottom)

        val output = Bitmap.createBitmap(outputSize, outputSize, Bitmap.Config.ARGB_8888)
        Canvas(output).drawBitmap(
            source,
            srcRect,
            Rect(0, 0, outputSize, outputSize),
            Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
        )
        return output
    }

    /** Compresses [bitmap] to JPEG bytes at [quality] (0-100). */
    fun toJpegBytes(bitmap: Bitmap, quality: Int = JPEG_QUALITY): ByteArray =
        ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            out.toByteArray()
        }

    /**
     * Crops [source] for the given framing and returns 512x512 JPEG bytes ready for upload.
     * Runs on [Dispatchers.Default].
     */
    suspend fun cropToJpeg(
        source: Bitmap,
        zoom: Float,
        offsetX: Float,
        offsetY: Float
    ): ByteArray = withContext(Dispatchers.Default) {
        val cropped = cropSquare(source, zoom, offsetX, offsetY)
        try {
            toJpegBytes(cropped)
        } finally {
            cropped.recycle()
        }
    }
}
