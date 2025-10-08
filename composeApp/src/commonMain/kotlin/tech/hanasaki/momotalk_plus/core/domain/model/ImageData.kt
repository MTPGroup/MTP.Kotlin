package tech.hanasaki.momotalk_plus.core.domain.model

data class ImageData(
    val fileName: String,
    val byteArray: ByteArray,
    val mimeType: String = "image/jpeg",
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ImageData

        if (fileName != other.fileName) return false
        if (!byteArray.contentEquals(other.byteArray)) return false
        if (mimeType != other.mimeType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + byteArray.contentHashCode()
        result = 31 * result + mimeType.hashCode()
        return result
    }
}