import com.russhwolf.settings.Settings
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class SerializableCookie(
    val name: String,
    val value: String,
    val encoding: CookieEncoding = CookieEncoding.RAW,
    val maxAge: Int? = null,
    val expires: String? = null,
    val domain: String? = null,
    val path: String? = null,
    val secure: Boolean = false,
    val httpOnly: Boolean = false,
    val extensions: Map<String, String?> = emptyMap()
)

class LocalCookieStorage(private val settings: Settings) {
    private val json = Json { ignoreUnknownKeys = true }

    fun saveCookie(cookie: Cookie, name: String) {
        try {
            val serializableCookie = SerializableCookie(
                name = cookie.name,
                value = cookie.value,
                encoding = cookie.encoding,
                maxAge = cookie.maxAge,
                expires = cookie.expires?.toString(),
                domain = cookie.domain,
                path = cookie.path,
                secure = cookie.secure,
                httpOnly = cookie.httpOnly,
                extensions = cookie.extensions
            )
            val cookieJson = json.encodeToString(serializableCookie)
            settings.putString(name, cookieJson)
        } catch (e: Exception) {
            println("Error saving cookie: ${e.message}")
        }
    }

    fun getCookie(name: String): Cookie? {
        return try {
            val cookieJson = settings.getStringOrNull(name) ?: return null
            val serializableCookie = json.decodeFromString<SerializableCookie>(cookieJson)
            Cookie(
                name = serializableCookie.name,
                value = serializableCookie.value,
                encoding = serializableCookie.encoding,
                maxAge = serializableCookie.maxAge,
                expires = null,
                domain = serializableCookie.domain,
                path = serializableCookie.path,
                secure = serializableCookie.secure,
                httpOnly = serializableCookie.httpOnly,
                extensions = serializableCookie.extensions
            )
        } catch (e: Exception) {
            println("Error retrieving cookie: ${e.message}")
            null
        }
    }

    fun removeCookie(name: String) {
        settings.remove(name)
    }
}