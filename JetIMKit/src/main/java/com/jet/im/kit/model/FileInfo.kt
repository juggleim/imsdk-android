package com.jet.im.kit.model

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.message.MessageMetaArray
import com.sendbird.android.message.ThumbnailSize
import com.sendbird.android.message.UploadableFileInfo
import com.sendbird.android.params.FileMessageCreateParams
import com.sendbird.android.params.MultipleFilesMessageCreateParams
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.consts.StringSet
import com.jet.im.kit.interfaces.OnResultHandler
import com.jet.im.kit.internal.model.VoiceMetaInfo
import com.jet.im.kit.internal.tasks.JobResultTask
import com.jet.im.kit.internal.tasks.TaskQueue.addTask
import com.jet.im.kit.log.Logger
import com.jet.im.kit.utils.FileUtils
import com.jet.im.kit.utils.ImageUtils
import java.io.File
import java.io.IOException
import java.util.Locale
import java.util.concurrent.Future

@Suppress("MemberVisibilityCanBePrivate")
class FileInfo internal constructor(
    val path: String,
    val size: Int,
    val mimeType: String?,
    val fileName: String?,
    val uri: Uri,
    val thumbnailWidth: Int,
    val thumbnailHeight: Int,
    val thumbnailPath: String? = null,
    val cacheDir: File? = null,
    internal val voiceMetaInfo: VoiceMetaInfo? = null
) {
    val file: File

    init {
        file = File(path)
    }

    val thumbnailFile: File?
        get() {
            return thumbnailPath?.let {
                val file = File(it)
                if (file.exists()) {
                    return file
                } else null
            }
        }

    fun toFileParams(): FileMessageCreateParams {
        return FileMessageCreateParams().apply {
            mimeType = this@FileInfo.mimeType
            fileName = this@FileInfo.fileName
            fileSize = this@FileInfo.size
            file = this@FileInfo.file
            if (thumbnailWidth > 0 && thumbnailHeight > 0) {
                Logger.dev("++ image width : %s, image height : %s", thumbnailWidth, thumbnailHeight)
                thumbnailSizes = listOf(
                    ThumbnailSize(thumbnailWidth, thumbnailHeight),
                    ThumbnailSize(thumbnailWidth / 2, thumbnailHeight / 2)
                )
            }
            voiceMetaInfo?.let {
                val duration = listOf(it.duration.toString())
                val type = listOf(voiceMetaInfo.type)
                metaArrays = listOf(
                    MessageMetaArray(StringSet.KEY_VOICE_MESSAGE_DURATION, duration),
                    MessageMetaArray(StringSet.KEY_INTERNAL_MESSAGE_TYPE, type)
                )
            }
        }
    }

    private fun toUploadableFileInfo(): UploadableFileInfo {
        val thumbnailSizes = if (thumbnailWidth > 0 && thumbnailHeight > 0) {
            Logger.dev("++ image width : %s, image height : %s", thumbnailWidth, thumbnailHeight)
            listOf(
                ThumbnailSize(thumbnailWidth, thumbnailHeight),
                ThumbnailSize(thumbnailWidth / 2, thumbnailHeight / 2)
            )
        } else listOf()
        return UploadableFileInfo(
            file,
            fileName,
            mimeType,
            size,
            thumbnailSizes
        )
    }

    fun clear() {
        Logger.d(">> FileInfo::clear()")
        if (path.isNotEmpty()) {
            val file = File(path)
            if (file.exists()) {
                val deleted = file.delete()
                Logger.d("-- file delete=%s, path=%s", deleted, path)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val fileInfo = other as FileInfo
        return uri == fileInfo.uri
    }

    override fun hashCode(): Int {
        return uri.hashCode()
    }

    override fun toString(): String {
        return "FileInfo{" +
            "path='" + path + '\'' +
            ", size=" + size +
            ", mimeType='" + mimeType + '\'' +
            ", fileName='" + fileName + '\'' +
            ", uri=" + uri +
            ", thumbnailWidth=" + thumbnailWidth +
            ", thumbnailHeight=" + thumbnailHeight +
            '}'
    }

    companion object {
        private const val MAX_COMPRESS_QUALITY = 100

        @JvmStatic
        fun toMultipleFilesParams(fileInfos: List<FileInfo>): MultipleFilesMessageCreateParams {
            return MultipleFilesMessageCreateParams(
                fileInfos.map {
                    it.toUploadableFileInfo()
                }
            )
        }

        private fun isCompressible(mimeType: String): Boolean {
            return mimeType.startsWith(StringSet.image) &&
                (mimeType.endsWith(StringSet.jpeg) || mimeType.endsWith(StringSet.jpg) || mimeType.endsWith(StringSet.png))
        }

        @JvmStatic
        fun fromVoiceFileInfo(voiceMessageInfo: VoiceMessageInfo, cacheDir: File): FileInfo {
            return with(File(voiceMessageInfo.path)) {
                val fileSize = (length() / 1024).toString().toInt()
                val voiceMetaInfo = VoiceMetaInfo(StringSet.voice + "/" + StringSet.m4a, voiceMessageInfo.duration)
                FileInfo(
                    voiceMessageInfo.path,
                    fileSize,
                    voiceMessageInfo.mimeType,
                    StringSet.Voice_message + "." + StringSet.m4a,
                    Uri.parse(voiceMessageInfo.path),
                    0,
                    0,
                    null,
                    cacheDir,
                    voiceMetaInfo
                )
            }
        }

        @JvmStatic
        fun fromUri(
            context: Context,
            uri: Uri,
            useImageCompression: Boolean,
            handler: OnResultHandler<FileInfo>?
        ): Future<FileInfo> {
            return addTask(object : JobResultTask<FileInfo>() {
                @Throws(IOException::class)
                override fun call(): FileInfo? {
                    return uriToFileInfo(context, uri, useImageCompression)
                }

                override fun onResultForUiThread(result: FileInfo?, e: SendbirdException?) {
                    if (e != null || result == null) {
                        Logger.w(e)
                        handler?.onError(e)
                        return
                    }
                    handler?.onResult(result)
                }
            })
        }

        @JvmStatic
        fun fromUris(
            context: Context,
            uris: List<Uri>,
            useImageCompression: Boolean,
            handler: OnResultHandler<List<FileInfo?>>?
        ): Future<List<FileInfo?>> {
            return addTask(object : JobResultTask<List<FileInfo?>>() {
                @Throws(IOException::class)
                override fun call(): List<FileInfo?>? {
                    return uris.map { uriToFileInfo(context, it, useImageCompression) }
                        .takeIf { it.isNotEmpty() }
                }

                override fun onResultForUiThread(result: List<FileInfo?>?, e: SendbirdException?) {
                    if (e != null || result == null) {
                        Logger.w(e)
                        handler?.onError(e)
                        return
                    }
                    handler?.onResult(result)
                }
            })
        }

        @VisibleForTesting
        @JvmStatic
        @WorkerThread
        @Throws(IOException::class)
        fun uriToFileInfo(context: Context, uri: Uri, useImageCompression: Boolean): FileInfo? {
            var fileInfo: FileInfo? = null
            context.contentResolver.query(uri, null, null, null, null).use { cursor ->
                val mimeType = context.contentResolver.getType(uri)
                var path = FileUtils.uriToPath(context, uri)
                val originPath = path
                val resizingSize = SendbirdUIKit.getResizingSize()
                if (cursor != null) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    var thumbnailPath = path
                    var thumbnailWidth = resizingSize.first / 2
                    var thumbnailHeight = resizingSize.second / 2
                    if (cursor.moveToFirst()) {
                        val name = cursor.getString(nameIndex)
                        var size = cursor.getLong(sizeIndex).toInt()
                        if (useImageCompression && mimeType != null && isCompressible(mimeType)) {
                            val quality = SendbirdUIKit.getCompressQuality()
                            require(!(quality < 0 || quality > MAX_COMPRESS_QUALITY)) { "quality must be 0..100" }
                            Logger.d("++ file size=%s, size from db=%s", File(path).length(), size)
                            val originSize = size
                            path = resizeImage(
                                context,
                                originPath,
                                mimeType,
                                quality,
                                resizingSize.first,
                                resizingSize.second
                            )
                            size = File(path).length().toInt()
                            Logger.d("++ originFile size=%s, resized file size=%s", originSize, size)
                            Logger.d("\n++ originFile path=%s, \n resized file path=%s\n", originPath, path)
                            if (originPath != path && size != originSize) {
                                Logger.d("++ file has been resized. the original file will remove.")
                                File(originPath).delete()
                            }
                        }
                        if (mimeType != null) {
                            val dimension = ImageUtils.getDimensions(path, mimeType)
                            thumbnailPath = path
                            thumbnailWidth = dimension.first
                            thumbnailHeight = dimension.second
                        }
                        Logger.d("==============================================================================")
                        Logger.d("++ FILE PATH : %s", path)
                        Logger.d("++ SIZE : %s", size)
                        Logger.d("++ MIMETYPE : %s", mimeType)
                        Logger.d("++ NAME : %s", name)
                        Logger.d("++ THUMBNAIL PATH : %s", thumbnailPath)
                        Logger.d("++ THUMBNAIL HEIGHT : %s", thumbnailWidth)
                        Logger.d("++ THUMBNAIL HEIGHT : %s", thumbnailHeight)
                        Logger.d("==============================================================================")
                        fileInfo = FileInfo(
                            path,
                            size,
                            mimeType,
                            name,
                            uri,
                            thumbnailWidth,
                            thumbnailHeight,
                            thumbnailPath,
                        )
                    }
                }
            }
            return fileInfo
        }

        @Throws(IOException::class)
        private fun resizeImage(
            context: Context,
            path: String,
            mimeType: String,
            quality: Int,
            width: Int,
            height: Int
        ): String {
            val inSampleSize = ImageUtils.calculateInSampleSize(path, width, height)

            // When it comes to compressing is ignored if the image's mine-type is a PNG.
            // inSampleSize is a 1 meant that the bitmap resizing is not necessary.
            if (inSampleSize > 1 || !mimeType.endsWith(StringSet.png) && quality < MAX_COMPRESS_QUALITY) {
                val originFile = File(path)
                val tempFileName = String.format(Locale.US, "Resized_%s_%s", quality, originFile.name)
                val destFile = FileUtils.createCachedDirFile(context, tempFileName)
                if (destFile.exists() && destFile.length() > 0) {
                    Logger.d("++ resized file exists")
                    return destFile.absolutePath
                }
                val bitmap = ImageUtils.getBitmap(path, width, height)
                Logger.d("++ resized image with=%s, height=%s", bitmap.width, bitmap.height)
                return FileUtils.bitmapToFile(
                    bitmap,
                    destFile,
                    quality,
                    FileUtils.extractBitmapFormat(mimeType)
                ).absolutePath
            }
            return path
        }
    }
}
