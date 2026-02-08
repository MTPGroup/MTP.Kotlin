package tech.hanasaki.azusa.shared.port.out

interface FileStoragePort {
    fun upload(objectKey: String, contentType: String, bytes: ByteArray): String
    fun delete(objectKey: String)
    fun publicUrl(objectKey: String): String
    fun download(objectKey: String): ByteArray
}
