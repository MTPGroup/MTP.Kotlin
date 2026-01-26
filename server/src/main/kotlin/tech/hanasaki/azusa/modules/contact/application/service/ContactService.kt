package tech.hanasaki.azusa.modules.contact.application.service

import tech.hanasaki.azusa.common.kernel.exception.ConflictException
import tech.hanasaki.azusa.common.kernel.exception.NotFoundException
import tech.hanasaki.azusa.common.kernel.model.CharacterId
import tech.hanasaki.azusa.common.kernel.model.PageResult
import tech.hanasaki.azusa.common.kernel.model.UserId
import tech.hanasaki.azusa.modules.contact.application.command.AddContactCommand
import tech.hanasaki.azusa.modules.contact.application.command.UpdateContactCommand
import tech.hanasaki.azusa.modules.contact.domain.model.Contact
import tech.hanasaki.azusa.modules.contact.domain.repository.ContactRepository

class ContactService(
    private val contactRepository: ContactRepository,
) {
    /**
     * 添加联系人
     */
    suspend fun addContact(cmd: AddContactCommand): Contact {
        val exists = contactRepository.find(cmd.userId, cmd.characterId)
        if (exists != null) throw ConflictException("Contact already exists")
        val contact = Contact.create(cmd.userId, cmd.characterId, cmd.nickname)
        contactRepository.save(contact)
        return contact
    }

    /**
     * 获取联系人
     */
    suspend fun getContact(userId: UserId, characterId: CharacterId): Contact {
        return contactRepository.find(userId, characterId)
            ?: throw NotFoundException("Contact not found")
    }

    /**
     * 获取自己的联系人列表
     */
    suspend fun listMyContacts(
        userId: UserId,
        page: Int,
        limit: Int,
    ): PageResult<Contact> {
        return contactRepository.findByUserIdPaged(userId, page, limit)
    }

    /**
     * 更新联系人昵称
     */
    suspend fun updateContactNickname(cmd: UpdateContactCommand): Contact {
        val contact = contactRepository.find(cmd.userId, cmd.characterId)
            ?: throw NotFoundException("Contact not found")
        contact.updateNickname(cmd.nickname)
        contactRepository.save(contact)
        return contact
    }

    /**
     * 删除联系人
     */
    suspend fun deleteContact(userId: UserId, characterId: CharacterId) {
        contactRepository.delete(userId, characterId)
    }
}