package tech.hanasaki.momotalk_plus.core.domain.repository

import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.core.domain.model.Character
import tech.hanasaki.momotalk_plus.core.domain.model.Visibility

interface CharacterRepository {
    /**
     * 创建角色
     *
     * @param name 角色名称
     * @param creatorId 创建者ID
     * @param persona 角色设定
     * @param signature 角色签名
     * @param avatarUrl 角色头像URL
     * @param visibility 角色可见性
     */
    suspend fun createCharacter(
        name: String,
        creatorId: String,
        persona: String,
        signature: String,
        avatarUrl: String,
        visibility: Visibility,
    )

    /**
     * 删除角色
     *
     * @param id 角色ID
     */
    suspend fun deleteCharacter(id: String)

    /**
     * 获取可用角色列表
     */
    fun getAvailableCharacters(): Flow<List<Character>>

    /**
     * 查询角色详情
     *
     * @param id 角色ID
     */
    fun getCharacterById(id: String): Flow<Character?>

    /**
     * 更新角色信息
     *
     * @param id 角色ID
     * @param name 角色名称
     * @param persona 角色设定
     * @param signature 角色签名
     * @param avatarUrl 角色头像URL
     * @param visibility 角色可见性
     */
    suspend fun updateCharacter(
        id: String,
        name: String,
        persona: String,
        signature: String,
        avatarUrl: String,
        visibility: Visibility,
    )
}