package tech.hanasaki.momotalk_plus.features.contacts.domain.repository

import tech.hanasaki.momotalk_plus.core.domain.model.AppError
import tech.hanasaki.momotalk_plus.core.domain.model.IResult
import tech.hanasaki.momotalk_plus.features.contacts.domain.model.Contact

interface ContactRepository {
    /**
     * 添加联系人
     *
     * @param characterId 角色ID
     *
     * @return IResult<[Unit], [AppError]>
     */
    suspend fun addContact(characterId: String): IResult<Unit, AppError>

    /**
     * 删除联系人
     *
     * @param characterId 角色ID
     *
     * @return IResult<[Unit], [AppError]>
     */
    suspend fun deleteContact(characterId: String): IResult<Unit, AppError>

    /**
     * 获取联系人列表
     *
     * @return IResult<List<[Contact]>, [AppError]>
     */
    suspend fun getContacts(): IResult<List<Contact>, AppError>
}