package com.example.album.app.album.data

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.example.common.network.repository.requestAffair
import com.example.common.network.repository.withHandling
import com.example.framework.utils.function.doOnDestroy
import com.example.album.Album
import com.example.album.model.AlbumFile
import com.example.album.model.AlbumFolder
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * 相册整体任何类
 */
class AlbumTask(private val owner: LifecycleOwner) {
    private var readerJob: Job? = null
    private var conversionJob: Job? = null
    val reader by lazy { MutableLiveData<Pair<ArrayList<AlbumFolder>, ArrayList<AlbumFile>>>() }
    val conversion by lazy { MutableLiveData<AlbumFile>() }

    init {
        owner.doOnDestroy {
            readerJob?.cancel()
            conversionJob?.cancel()
        }
    }

    /**
     * 相册扫描
     * @mFunction 扫描模式：图片 / 视频 / 全部
     * @mCheckedFiles 已经选中的文件（用于回显勾选状态）
     * @mMediaReader 媒体扫描器
     */
    fun mediaReaderExecute(mFunction: Int, mCheckedFiles: ArrayList<AlbumFile>?, mMediaReader: MediaReader) {
        readerJob?.cancel()
        readerJob = owner.lifecycleScope.launch(Main.immediate) {
            flow {
                emit(requestAffair {
                    // 根据模式调用扫描 -> 所有文件夹
                    val albumFolders = when (mFunction) {
                        Album.FUNCTION_CHOICE_IMAGE -> mMediaReader.getAllImage()
                        Album.FUNCTION_CHOICE_VIDEO -> mMediaReader.getAllVideo()
                        Album.FUNCTION_CHOICE_ALBUM -> mMediaReader.getAllMedia()
                        else -> throw AssertionError("This should not be the case.")
                    }
                    // 已选中的文件 -> 整理
                    val checkedFiles = ArrayList<AlbumFile>()
                    if (!mCheckedFiles.isNullOrEmpty()) {
                        // 拿到“全部图片/视频”文件夹里的文件
                        val albumFiles = albumFolders[0].albumFiles
                        // 遍历对比，把之前选中的文件重新勾选
                        for (checkAlbumFile in mCheckedFiles) {
                            for (i in albumFiles.indices) {
                                val albumFile = albumFiles[i]
                                if (checkAlbumFile == albumFile) {
                                    albumFile.isChecked = true
                                    checkedFiles.add(albumFile)
                                }
                            }
                        }
                    }
                    albumFolders to checkedFiles
                })
            }.withHandling(err = {
                reader.postValue(arrayListOf<AlbumFolder>() to arrayListOf<AlbumFile>())
            }).collect { (folders, files) ->
                reader.postValue(folders to files)
            }
        }
    }

    /**
     * 拍摄结束,对文件路径做一个转换
     */
    fun pathConversionExecute(mConversion: PathConversion, result: String) {
        conversionJob?.cancel()
        conversionJob = owner.lifecycleScope.launch(Main.immediate) {
            flow {
                emit(requestAffair { mConversion.convert(result) })
            }.withHandling(err = {
                conversion.postValue(AlbumFile())
            }).collect {
                conversion.postValue(it)
            }
        }
    }

}