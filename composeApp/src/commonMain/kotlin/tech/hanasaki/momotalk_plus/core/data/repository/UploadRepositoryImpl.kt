package tech.hanasaki.momotalk_plus.core.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import tech.hanasaki.momotalk_plus.core.domain.model.ImageData
import tech.hanasaki.momotalk_plus.core.domain.model.UploadPath
import tech.hanasaki.momotalk_plus.core.domain.repository.UploadImageRepository

class UploadImageRepositoryImpl(
    private val supabase: SupabaseClient,
) : UploadImageRepository {
    override suspend fun uploadImage(
        imageData: ImageData,
        path: UploadPath,
        userId: String?,
    ): String {
        val pathString = when (path) {
            UploadPath.AVATAR -> "avatar"
            UploadPath.BACKGROUND -> "background"
            UploadPath.GENERAL -> "general"
        }

        val bucket = supabase.storage.from(pathString)
        return bucket.upload(
            imageData.fileName,
            imageData.byteArray,
        ).run {
            bucket.publicUrl(this.path)
        }
    }
}