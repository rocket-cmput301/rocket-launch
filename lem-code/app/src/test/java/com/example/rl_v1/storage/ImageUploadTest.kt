package com.example.rl_v1.storage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test


class ImageUploadTest {

    // ---- storage paths ----

    @Test fun folderPathIsPerUser() {
        assertEquals("profile_pictures/abc123", ProfilePicturePaths.folderPath("abc123"))
    }

    @Test fun differentUsersGetDifferentFolders() {
        assertNotEquals(ProfilePicturePaths.folderPath("userA"), ProfilePicturePaths.folderPath("userB"))
    }

    @Test fun fileNameUsesTimestampAndJpgExtension() {
        assertEquals("profile_1700000000000.jpg", ProfilePicturePaths.fileName(1_700_000_000_000L))
    }

    @Test fun eachUploadGetsANewFileName() {
        assertNotEquals(ProfilePicturePaths.fileName(1000L), ProfilePicturePaths.fileName(1001L))
    }

    @Test fun uploadedPicturesAreJpeg() {
        assertEquals("image/jpeg", ProfilePicturePaths.JPEG_CONTENT_TYPE)
    }

    // ---- cleanup of old pictures ----

    @Test fun cleanupKeepsTheNewUploadAndDeletesTheRest() {
        val names = listOf("profile_1.jpg", "profile_2.jpg", "profile_3.jpg")
        assertEquals(
            listOf("profile_1.jpg", "profile_2.jpg"),
            ProfilePicturePaths.filesToDelete(names, keepName = "profile_3.jpg")
        )
    }

    @Test fun removingThePhotoDeletesEverything() {
        val names = listOf("profile_1.jpg", "profile_2.jpg")
        assertEquals(names, ProfilePicturePaths.filesToDelete(names, keepName = null))
    }

    @Test fun cleanupOfEmptyFolderDeletesNothing() {
        assertEquals(emptyList<String>(), ProfilePicturePaths.filesToDelete(emptyList(), "profile_1.jpg"))
    }

    @Test fun cleanupWithOnlyTheNewFileDeletesNothing() {
        assertEquals(
            emptyList<String>(),
            ProfilePicturePaths.filesToDelete(listOf("profile_9.jpg"), keepName = "profile_9.jpg")
        )
    }

    // ---- offset clamping ----

    @Test fun squareImageAtCoverFitCannotMove() {
        assertEquals(0f to 0f, ImageCropper.maxOffsets(500, 500, zoom = 1f))
    }

    @Test fun zoomingInAllowsMovement() {
        assertEquals(0.5f to 0.5f, ImageCropper.maxOffsets(500, 500, zoom = 2f))
    }

    @Test fun landscapeImageCanMoveSidewaysOnly() {
        assertEquals(0.5f to 0f, ImageCropper.maxOffsets(1000, 500, zoom = 1f))
    }

    @Test fun portraitImageCanMoveUpDownOnly() {
        assertEquals(0f to 0.5f, ImageCropper.maxOffsets(400, 800, zoom = 1f))
    }

    @Test fun zoomBelowCoverFitNeverGivesNegativeLimits() {
        assertEquals(0f to 0f, ImageCropper.maxOffsets(500, 500, zoom = 0.5f))
    }

    @Test fun maxZoomIsFourTimes() {
        assertEquals(4f, ImageCropper.MAX_ZOOM)
    }

    // ---- crop region ----

    @Test fun coverFitCropsCentredSquareOfLandscape() {
        assertEquals(
            ImageCropper.CropRegion(250, 0, 750, 500),
            ImageCropper.cropRegion(1000, 500, zoom = 1f, offsetX = 0f, offsetY = 0f)
        )
    }

    @Test fun coverFitCropsCentredSquareOfPortrait() {
        assertEquals(
            ImageCropper.CropRegion(0, 200, 400, 600),
            ImageCropper.cropRegion(400, 800, zoom = 1f, offsetX = 0f, offsetY = 0f)
        )
    }

    @Test fun zoomingInCropsASmallerCentredSquare() {
        assertEquals(
            ImageCropper.CropRegion(375, 125, 625, 375),
            ImageCropper.cropRegion(1000, 500, zoom = 2f, offsetX = 0f, offsetY = 0f)
        )
    }

    @Test fun movingImageRightShowsItsLeftSide() {
        assertEquals(
            ImageCropper.CropRegion(0, 0, 500, 500),
            ImageCropper.cropRegion(1000, 500, zoom = 1f, offsetX = 0.5f, offsetY = 0f)
        )
    }

    @Test fun movingImageLeftWhileZoomedShowsItsRightSide() {
        // 800x800 at 4x: a 200px square, moved 0.2 diameters left -> centre 40px right of middle.
        assertEquals(
            ImageCropper.CropRegion(340, 300, 540, 500),
            ImageCropper.cropRegion(800, 800, zoom = 4f, offsetX = -0.2f, offsetY = 0f)
        )
    }

    @Test fun maxOffsetPutsCropExactlyOnTheImageEdge() {
        val (maxX, _) = ImageCropper.maxOffsets(1000, 500, zoom = 2f)
        val region = ImageCropper.cropRegion(1000, 500, zoom = 2f, offsetX = maxX, offsetY = 0f)
        assertEquals(0, region.left)
    }

    @Test fun offsetsBeyondTheLimitStayInsideTheImage() {
        val region = ImageCropper.cropRegion(1000, 500, zoom = 1f, offsetX = 5f, offsetY = -5f)
        assertEquals(ImageCropper.CropRegion(0, 0, 500, 500), region)
    }

    @Test fun cropRegionIsAlwaysSquareAndInsideTheImage() {
        val sizes = listOf(1000 to 500, 400 to 800, 777 to 777, 1536 to 1024)
        val zooms = listOf(1f, 1.7f, 2.5f, ImageCropper.MAX_ZOOM)
        val offsets = listOf(-1f, -0.3f, 0f, 0.25f, 1f)
        for ((w, h) in sizes) for (zoom in zooms) for (dx in offsets) for (dy in offsets) {
            val r = ImageCropper.cropRegion(w, h, zoom, dx, dy)
            val label = "${w}x$h zoom=$zoom offset=($dx, $dy) -> $r"
            assertTrue(label, r.left >= 0 && r.top >= 0 && r.right <= w && r.bottom <= h)
            assertTrue(label, kotlin.math.abs(r.width - r.height) <= 1)
            assertTrue(label, r.width >= 1)
        }
    }
}
