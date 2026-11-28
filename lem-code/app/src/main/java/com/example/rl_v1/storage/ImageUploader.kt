package com.example.rl_v1.storage

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await

/**
 * Uploads and removes user images (currently profile pictures) in remote storage.
 */
interface ImageUploader {
    /**
     * Uploads the image at [imageUri] as the profile picture for [userId], replacing any previous one.
     *
     * @return The public download URL of the uploaded image.
     */
    suspend fun uploadProfilePicture(userId: String, imageUri: Uri): String

    /**
     * Uploads already-encoded JPEG [jpegBytes] as the profile picture for [userId], replacing any
     * previous one.
     *
     * @return The public download URL of the uploaded image.
     */
    suspend fun uploadProfilePicture(userId: String, jpegBytes: ByteArray): String

    /**
     * Deletes the stored profile picture(s) for [userId]. Does nothing if there are none.
     */
    suspend fun deleteProfilePicture(userId: String)
}

/**
 * [ImageUploader] backed by Firebase Storage. Each user's picture is stored in
 * `profile_pictures/{userId}/`, where `userId` is the same id used for the user's Firestore
 * document (the device identifier).
 *
 * Every upload creates a new file name (`profile_<timestamp>.jpg`) so its download URL is new too,
 * which stops image loaders from showing a cached copy of the previous picture. Older pictures in
 * the folder are deleted after a successful upload.
 *
 * @param storage The [FirebaseStorage] instance to upload to.
 */
class FirebaseImageUploader(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : ImageUploader {

    /** Storage folder holding [userId]'s profile picture. */
    private fun profileFolderRef(userId: String): StorageReference =
        storage.reference.child("$PROFILE_PICTURES_DIR/$userId")

    override suspend fun uploadProfilePicture(userId: String, imageUri: Uri): String =
        // Content type is inferred from the picked file by the Storage SDK.
        upload(userId) { ref -> ref.putFile(imageUri).await() }

    override suspend fun uploadProfilePicture(userId: String, jpegBytes: ByteArray): String {
        require(jpegBytes.isNotEmpty()) { "Image data must not be empty." }
        val metadata = StorageMetadata.Builder().setContentType("image/jpeg").build()
        return upload(userId) { ref -> ref.putBytes(jpegBytes, metadata).await() }
    }

    /**
     * Uploads to a new file in [userId]'s folder using [put], then removes older pictures.
     *
     * @return The download URL of the new file.
     */
    private suspend fun upload(userId: String, put: suspend (StorageReference) -> Unit): String {
        require(userId.isNotBlank()) { "userId must not be blank." }
        val folder = profileFolderRef(userId)
        val ref = folder.child("profile_${System.currentTimeMillis()}.jpg")
        put(ref)
        val downloadUrl = ref.downloadUrl.await().toString()
        deleteOthers(folder, keepName = ref.name)
        return downloadUrl
    }

    override suspend fun deleteProfilePicture(userId: String) {
        deleteOthers(profileFolderRef(userId), keepName = null)
    }

    /** Best-effort removal of every file in [folder] except [keepName]; failures are only logged. */
    private suspend fun deleteOthers(folder: StorageReference, keepName: String?) {
        try {
            folder.listAll().await().items
                .filter { it.name != keepName }
                .forEach { item ->
                    try {
                        item.delete().await()
                    } catch (e: StorageException) {
                        if (e.errorCode != StorageException.ERROR_OBJECT_NOT_FOUND) {
                            Log.w(TAG, "Could not delete old profile picture ${item.path}", e)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Could not clean up profile pictures in ${folder.path}", e)
        }
    }

    private companion object {
        const val TAG = "FirebaseImageUploader"
        const val PROFILE_PICTURES_DIR = "profile_pictures"
    }
}
