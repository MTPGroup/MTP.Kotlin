package tech.hanasaki.azusa.modules.character.application.port.`in`.dto

import kotlin.uuid.Uuid


data class CharacterAuthorView(
    val id: Uuid,
    val name: String,
    val avatar: String?,
)

data class CharacterView(
    val id: Uuid,
    val authorId: Uuid,
    val author: CharacterAuthorView?,
    val name: String,
    val avatar: String?,
    val bio: String?,
    val originPrompt: String?,
    val isPublic: Boolean,
    val createdAt: String,
    val updatedAt: String,
)
