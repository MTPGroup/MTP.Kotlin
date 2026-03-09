package tech.hanasaki.azusa.modules.character.adapter.out.persistence.mapper

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import tech.hanasaki.azusa.modules.character.adapter.out.persistence.table.CharacterTable
import tech.hanasaki.azusa.modules.character.domain.model.Character
import tech.hanasaki.azusa.modules.character.domain.model.CharacterExampleMessage
import tech.hanasaki.azusa.shared.domain.model.vo.AvatarUrl
import tech.hanasaki.azusa.shared.domain.model.vo.CharacterId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object CharacterMapper {
    fun toDomain(row: ResultRow): Character = Character.reconstitute(
        id = CharacterId(row[CharacterTable.id]),
        authorId = UserId(row[CharacterTable.authorId]),
        name = row[CharacterTable.name],
        avatar = row[CharacterTable.avatar]?.let { AvatarUrl(it) },
        bio = row[CharacterTable.bio],
        tags = parseTags(row[CharacterTable.tags]),
        exampleMessages = parseExampleMessages(row[CharacterTable.exampleMessages]),
        originPrompt = row[CharacterTable.originPrompt],
        isPublic = row[CharacterTable.isPublic],
        createdAt = row[CharacterTable.createdAt],
        updatedAt = row[CharacterTable.updatedAt],
    )

    fun toEntity(domain: Character, target: UpdateBuilder<*>) {
        target[CharacterTable.authorId] = domain.authorId.value
        target[CharacterTable.name] = domain.name
        target[CharacterTable.avatar] = domain.avatar?.value
        target[CharacterTable.bio] = domain.bio
        target[CharacterTable.tags] = domain.tags.joinToString(",")
        target[CharacterTable.exampleMessages] = encodeExampleMessages(domain.exampleMessages)
        target[CharacterTable.originPrompt] = domain.originPrompt
        target[CharacterTable.isPublic] = domain.isPublic
    }

    private fun parseTags(raw: String): List<String> =
        raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    private fun parseExampleMessages(raw: String): List<CharacterExampleMessage> {
        if (raw.isBlank()) return emptyList()
        val element = runCatching { Json.parseToJsonElement(raw) }.getOrNull() ?: return emptyList()
        val array = runCatching { element.jsonArray }.getOrNull() ?: return emptyList()
        return array.mapNotNull { json ->
            val obj = runCatching { json.jsonObject }.getOrNull() ?: return@mapNotNull null
            val role = obj["role"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            val content = obj["content"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            CharacterExampleMessage(role = role, content = content)
        }
    }

    private fun encodeExampleMessages(messages: List<CharacterExampleMessage>): String =
        buildJsonArray {
            messages.forEach {
                add(
                    buildJsonObject {
                        put("role", JsonPrimitive(it.role))
                        put("content", JsonPrimitive(it.content))
                    }
                )
            }
        }.toString()
}
