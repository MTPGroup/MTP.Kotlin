package tech.hanasaki.azusa.shared.port.out

interface FileStoragePort {
    fun uploadAvatar(objectKey: String, contentType: String, bytes: ByteArray): String
    fun uploadFile(objectKey: String, contentType: String, bytes: ByteArray): String
    fun deleteFile(objectKey: String)
}
