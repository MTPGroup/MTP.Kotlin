package tech.hanasaki.momotalk_plus.core.domain.repository

import tech.hanasaki.momotalk_plus.core.domain.model.AppError
import tech.hanasaki.momotalk_plus.core.domain.model.IResult
import tech.hanasaki.momotalk_plus.core.domain.model.ImageData
import tech.hanasaki.momotalk_plus.core.domain.model.UploadPath

interface UploadImageRepository {
    /**
     * 上传图片至服务器
     * @param imageData 本地图片
     * @param path 上传路径
     * @param userId 用户ID, 可选
     *
     * @return IResult<[String], [AppError]> 成功返回图片URL, 失败返回错误信息
     */
    suspend fun uploadImage(
        imageData: ImageData,
        path: UploadPath,
        userId: String? = null,
    ): IResult<String, AppError>
}