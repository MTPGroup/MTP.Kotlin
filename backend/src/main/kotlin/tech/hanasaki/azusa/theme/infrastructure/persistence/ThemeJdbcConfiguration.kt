package tech.hanasaki.azusa.theme.infrastructure.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import org.postgresql.util.PGobject
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import tech.hanasaki.azusa.theme.domain.model.ThemeDefinition

@Configuration
class ThemeJdbcConfiguration(
    private val objectMapper: ObjectMapper,
) : AbstractJdbcConfiguration() {
    override fun userConverters(): List<*> {
        return listOf(
            ThemeDefinitionWritingConverter(objectMapper),
            ThemeDefinitionReadingConverter(objectMapper)
        )
    }
}

/**
 * 写入转换器：ThemeDefinition -> PostgreSQL JSONB (PGobject)
 */
@WritingConverter
class ThemeDefinitionWritingConverter(
    private val objectMapper: ObjectMapper,
) : Converter<ThemeDefinition, PGobject> {
    override fun convert(source: ThemeDefinition): PGobject {
        val jsonObject = PGobject()
        jsonObject.type = "jsonb"
        jsonObject.value = objectMapper.writeValueAsString(source)
        return jsonObject
    }
}

/**
 * 读取转换器：PostgreSQL JSONB (PGobject) -> ThemeDefinition
 */
@ReadingConverter
class ThemeDefinitionReadingConverter(
    private val objectMapper: ObjectMapper,
) : Converter<PGobject, ThemeDefinition> {
    override fun convert(source: PGobject): ThemeDefinition {
        return objectMapper.readValue(source.value, ThemeDefinition::class.java)
    }
}
