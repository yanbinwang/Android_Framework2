package com.yanzhenjie.album.app.album

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import com.example.common.utils.ScreenUtil.shouldUseWhiteSystemBarsForRes
import com.example.framework.utils.builder.TimerBuilder.Companion.schedule
import com.example.framework.utils.function.color
import com.example.framework.utils.function.hasExtras
import com.example.framework.utils.function.intentBoolean
import com.example.framework.utils.function.intentInt
import com.example.framework.utils.function.intentLong
import com.example.framework.utils.function.intentParcelable
import com.example.framework.utils.function.intentParcelableArrayList
import com.example.framework.utils.function.value.orFalse
import com.example.gallery.R
import com.example.gallery.base.BaseActivity
import com.yanzhenjie.album.Album
import com.yanzhenjie.album.app.Contract
import com.yanzhenjie.album.app.album.data.AlbumTask
import com.yanzhenjie.album.app.album.data.MediaReader
import com.yanzhenjie.album.app.album.data.PathConversion
import com.yanzhenjie.album.callback.Action
import com.yanzhenjie.album.callback.Filter
import com.yanzhenjie.album.model.AlbumFile
import com.yanzhenjie.album.model.AlbumFolder
import com.yanzhenjie.album.model.Widget
import com.yanzhenjie.album.utils.AlbumUtil.getMimeType
import com.yanzhenjie.album.utils.AlbumUtil.randomJPGPath
import com.yanzhenjie.album.utils.AlbumUtil.randomMP4Path
import com.yanzhenjie.album.utils.MediaScanner
import com.example.gallery.widget.LoadingDialog
import java.io.File

/**
 * 相册主页（总控 Activity）
 * 功能：扫描、选择、拍照、预览、文件夹、回调、所有逻辑
 */
internal class AlbumActivity : BaseActivity(), Contract.AlbumPresenter {
    // 功能：图片/视频/全部
    private val mFunction by lazy { intentInt(Album.KEY_INPUT_FUNCTION) }
    // 单选/多选
    private val mChoiceMode by lazy { intentInt(Album.KEY_INPUT_CHOICE_MODE) }
    // 列表列数
    private val mColumnCount by lazy { intentInt(Album.KEY_INPUT_COLUMN_COUNT) }
    // 最大选择数量
    private val mLimitCount by lazy { intentInt(Album.KEY_INPUT_LIMIT_COUNT) }
    // 视频质量
    private val mQuality by lazy { intentInt(Album.KEY_INPUT_CAMERA_QUALITY) }
    // 视频最大时长
    private val mLimitDuration by lazy { intentLong(Album.KEY_INPUT_CAMERA_DURATION) }
    // 视频最大大小
    private val mLimitBytes by lazy { intentLong(Album.KEY_INPUT_CAMERA_BYTES) }
    // 是否显示拍照按钮
    private val mHasCamera by lazy { intentBoolean(Album.KEY_INPUT_ALLOW_CAMERA) }
    // 是否显示不可用文件
    private val mFilterVisibility by lazy { intentBoolean(Album.KEY_INPUT_FILTER_VISIBILITY) }
    // 主题样式
    private val mWidget by lazy { intentParcelable<Widget>(Album.KEY_INPUT_WIDGET) ?: Widget.getDefaultWidget(this) }

    // 相机选择弹窗
    private lateinit var mCameraPopupMenu: PopupMenu
    // 文件夹选择弹窗
    private lateinit var mFolderDialog: FolderDialog
    // 当前选中的文件夹
    private var mCurrentFolder = 0
    // 所有文件夹
    private var mAlbumFolders = ArrayList<AlbumFolder>()
    // 已选中的图片
    private var mCheckedList = ArrayList<AlbumFile>()
    // 加载对话框
    private val mLoadingDialog by lazy { LoadingDialog(this) }
    // 媒体扫描
    private val mMediaScanner by lazy { MediaScanner(this) }
    // 相册全局任务
    private val mTask by lazy { AlbumTask(this) }
    // MVP & UI
    private val mView by lazy { AlbumView(this, this) }

    /**
     * 拍照/录像完成 → 插入相册
     */
    private val mCameraAction = object : Action<String> {
        override fun onAction(result: String) {
            // 开始路径转换
            showLoadingDialog()
            /**
             * 1) 触发系统媒体数据库更新
             * Android 系统有个内置数据库，记录所有图片、视频、音频。你调用 scan() →系统把这个文件路径插入 / 更新到数据库里。
             * 这样相册才能看到新图片。
             * 2) 触发文件管理器 / 相册刷新
             * 扫描完成后 → 系统相册、文件管理器都会立刻看到新文件不用等重启、不用等多久。
             * 3) 触发 onScanCompleted 回调
             * 4) 基本等价于 insertImageResolver (项目扩展函数) 区别在于无法扫描包名文件夹下内部的图片
             */
            mMediaScanner.scan(result)
            // 同步执行路径转换
            val conversion = PathConversion(sSizeFilter, sMimeFilter, sDurationFilter)
            mTask.pathConversionExecute(conversion, result)
        }
    }

    companion object {
        // 空相册跳转
        private const val CODE_ACTIVITY_NULL = 1

        var sSizeFilter: Filter<Long>? = null
        var sDurationFilter: Filter<Long>? = null
        var sMimeFilter: Filter<String>? = null
        var sCancel: Action<String>? = null
        var sResult: Action<ArrayList<AlbumFile>>? = null
    }

    override fun isImmersionBarEnabled() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 校验参数
        if (!hasExtras()) return finish()
        // 根据主题加载布局
        setContentView(R.layout.album_activity_album)
        // 初始化状态栏
        val statusBarBattery = shouldUseWhiteSystemBarsForRes(mWidget.statusBarColor)
        val navigationBarBattery = shouldUseWhiteSystemBarsForRes(mWidget.navigationBarColor)
        initImmersionBar(!statusBarBattery, !navigationBarBattery, mWidget.navigationBarColor)
        // MVP设置
        mView.setupViews(mWidget, mColumnCount, mHasCamera, mChoiceMode)
        // 弹框设置
        val progressColor = color(if (mWidget.uiStyle == Widget.STYLE_LIGHT) {
            // 浅色模式 → 深色加载条
            R.color.albumLoading
        } else {
            // 深色模式 → 用主题色
            mWidget.statusBarColor
        })
        mLoadingDialog.setupViews(progressColor, R.string.album_converting)
        // 返回键 → 取消
        setOnBackPressedListener {
            callbackCancel()
        }
        // 扫描回调
        mTask.reader.observe {
            val (albumFolders, checkedFiles) = this
            mAlbumFolders = albumFolders
            mCheckedList = checkedFiles
            // 扫描完成回调
            val scanAction = { isNull: Boolean ->
                var delayMillis = 500L
                if (isNull) delayMillis = 1000L
                schedule(this@AlbumActivity, {
                    // 完成按钮是否显示
                    when (mChoiceMode) {
                        Album.MODE_MULTIPLE -> mView.setCompleteDisplay(true)
                        Album.MODE_SINGLE -> mView.setCompleteDisplay(false)
                        else -> throw AssertionError("This should not be the case.")
                    }
                    // 隐藏整体遮罩
                    mView.setLoadingDisplay(false)
                }, delayMillis)
            }
            // 没有图片 → 打开空页面
            if (mAlbumFolders[0].albumFiles.isEmpty()) {
                // 延迟1秒关闭 loading，过渡更自然
                schedule(this@AlbumActivity, {
                    val intent = Intent(this@AlbumActivity, NullActivity::class.java)
                    intent.putExtras(getIntent())
                    startActivityForResult(intent, CODE_ACTIVITY_NULL)
                    overridePendingTransition(R.anim.set_alpha_in, R.anim.set_alpha_out)
                    scanAction(true)
                })
            } else {
                // 显示全部图片
                showFolderAlbumFiles(0)
                val count = mCheckedList.size
                mView.setCheckedCount(count)
                scanAction(false)
            }
        }
        // 路径转换回调
        mTask.conversion.observe {
            val addFileToListAction = { file: AlbumFile ->
                if (mCurrentFolder != 0) {
                    val albumFiles = mAlbumFolders[0].albumFiles
                    if (!albumFiles.isEmpty()) {
                        albumFiles.add(0, file)
                    } else {
                        albumFiles.add(file)
                    }
                }
                val albumFolder = mAlbumFolders[mCurrentFolder]
                val albumFiles = albumFolder.albumFiles
                if (albumFiles.isEmpty()) {
                    albumFiles.add(file)
                    mView.bindAlbumFolder(albumFolder)
                } else {
                    albumFiles.add(0, file)
                    mView.notifyInsertItem(if (mHasCamera) 1 else 0)
                }
                mCheckedList.add(file)
                val count = mCheckedList.size
                mView.setCheckedCount(count)
                // 插入行为结束,给予1s动画转圈过渡
                schedule(this@AlbumActivity, {
                    if (mChoiceMode == Album.MODE_SINGLE) {
                        callbackResult()
                    } else {
                        dismissLoadingDialog()
                    }
                })
            }
            isChecked = !isDisable
            if (isDisable) {
                if (mFilterVisibility) {
                    addFileToListAction(this)
                } else {
                    mView.toast(getString(R.string.album_take_file_unavailable))
                    // 不可以直接取消弹框
                    dismissLoadingDialog()
                }
            } else {
                // 添加到列表
                addFileToListAction(this)
            }
        }
        // 开始扫描相册
        mView.setCompleteDisplay(false)
        mView.setLoadingDisplay(true)
        val checkedFiles = intentParcelableArrayList<AlbumFile>(Album.KEY_INPUT_CHECKED_LIST)
        val mediaReader = MediaReader(this, sSizeFilter, sMimeFilter, sDurationFilter, mFilterVisibility)
        mTask.mediaReaderExecute(mFunction, checkedFiles, mediaReader)
    }

    /**
     * 点击所有图片 -> 切换相册
     */
    override fun clickFolderSwitch() {
        if (!::mFolderDialog.isInitialized) {
            mFolderDialog = FolderDialog(this, mWidget, mAlbumFolders) { position: Int ->
                mCurrentFolder = position
                showFolderAlbumFiles(mCurrentFolder)
            }
        }
        if (!mFolderDialog.isShowing) {
            mFolderDialog.show()
        }
    }

    /**
     * 点击列表 -> 拍照/录像
     */
    override fun clickCamera(v: View?) {
        val hasCheckSize = mCheckedList.size
        // 超过最大选择数量 → 提示
        if (hasCheckSize >= mLimitCount) {
            val messageRes = when (mFunction) {
                Album.FUNCTION_CHOICE_IMAGE -> R.string.album_check_image_limit_camera
                Album.FUNCTION_CHOICE_VIDEO -> R.string.album_check_video_limit_camera
                Album.FUNCTION_CHOICE_ALBUM -> R.string.album_check_album_limit_camera
                else -> throw AssertionError("This should not be the case.")
            }
            mView.toast(getString(messageRes, mLimitCount))
            // 根据功能类型拍照/录像/选择
        } else {
            when (mFunction) {
                Album.FUNCTION_CHOICE_IMAGE -> takePicture()
                Album.FUNCTION_CHOICE_VIDEO -> takeVideo()
                Album.FUNCTION_CHOICE_ALBUM -> {
                    if (!::mCameraPopupMenu.isInitialized && null != v) {
                        mCameraPopupMenu = PopupMenu(this, v)
                        mCameraPopupMenu.menuInflater.inflate(R.menu.album_menu_item_camera, mCameraPopupMenu.menu)
                        mCameraPopupMenu.setOnMenuItemClickListener { item: MenuItem? ->
                            when (item?.itemId) {
                                R.id.album_menu_camera_image -> takePicture()
                                R.id.album_menu_camera_video -> takeVideo()
                            }
                            true
                        }
                    }
                    mCameraPopupMenu.show()
                }
                else -> throw AssertionError("This should not be the case.")
            }
        }
    }

    /**
     * 选择/取消选择图片
     */
    override fun tryCheckItem(button: CompoundButton?, position: Int) {
        val albumFile = mAlbumFolders[mCurrentFolder].albumFiles[position]
        if (button?.isChecked.orFalse) {
            if (mCheckedList.size >= mLimitCount) {
                val messageRes = when (mFunction) {
                    Album.FUNCTION_CHOICE_IMAGE -> R.string.album_check_image_limit
                    Album.FUNCTION_CHOICE_VIDEO -> R.string.album_check_video_limit
                    Album.FUNCTION_CHOICE_ALBUM -> R.string.album_check_album_limit
                    else -> throw AssertionError("This should not be the case.")
                }
                mView.toast(getString(messageRes, mLimitCount))
                button?.setChecked(false)
            } else {
                albumFile.isChecked = true
                mCheckedList.add(albumFile)
                setCheckedCount()
            }
        } else {
            albumFile.isChecked = false
            mCheckedList.remove(albumFile)
            setCheckedCount()
        }
    }

    /**
     * 预览图片
     */
    override fun tryPreviewItem(position: Int) {
        when (mChoiceMode) {
            Album.MODE_SINGLE -> {
                val albumFile = mAlbumFolders[mCurrentFolder].albumFiles[position]
                mCheckedList.add(albumFile)
                setCheckedCount()
                callbackResult()
            }
            Album.MODE_MULTIPLE -> {
                setPreview(mAlbumFolders[mCurrentFolder].albumFiles, position)
            }
            else -> {
                throw AssertionError("This should not be the case.")
            }
        }
    }

    /**
     * 预览已选中的图片
     */
    override fun tryPreviewChecked() {
        if (!mCheckedList.isEmpty()) {
            setPreview(mCheckedList, 0)
        }
    }

    /**
     * 点击完成
     */
    override fun complete() {
        if (mCheckedList.isEmpty()) {
            val messageRes = when (mFunction) {
                Album.FUNCTION_CHOICE_IMAGE -> R.string.album_check_image_little
                Album.FUNCTION_CHOICE_VIDEO -> R.string.album_check_video_little
                Album.FUNCTION_CHOICE_ALBUM -> R.string.album_check_album_little
                else -> throw AssertionError("This should not be the case.")
            }
            mView.toast(messageRes)
        } else {
            callbackResult()
        }
    }

    /**
     * 浏览页跳转
     */
    private fun setPreview(albumFiles: ArrayList<AlbumFile>, currentPosition: Int) {
        AlbumPreviewActivity.sAlbumFiles = albumFiles
        AlbumPreviewActivity.sCheckedCount = mCheckedList.size
        AlbumPreviewActivity.sCurrentPosition = currentPosition
        AlbumPreviewActivity.sCallback = object : AlbumPreviewActivity.Callback {
            override fun onPreviewComplete() {
                callbackResult()
            }

            override fun onPreviewChanged(albumFile: AlbumFile) {
                val albumFiles = mAlbumFolders[mCurrentFolder].albumFiles
                val position = albumFiles.indexOf(albumFile)
                val notifyPosition = if (mHasCamera) position + 1 else position
                mView.notifyItem(notifyPosition)
                if (albumFile.isChecked) {
                    if (!mCheckedList.contains(albumFile)) {
                        mCheckedList.add(albumFile)
                    }
                } else {
                    if (mCheckedList.contains(albumFile)) {
                        mCheckedList.remove(albumFile)
                    }
                }
                setCheckedCount()
            }
        }
        val intent = Intent(this, AlbumPreviewActivity::class.java)
        intent.putExtras(getIntent())
        startActivity(intent)
    }

    /**
     * 更新已选数量
     */
    private fun setCheckedCount() {
        val count = mCheckedList.size
        mView.setCheckedCount(count)
    }

    /**
     * 拍照
     */
    private fun takePicture() {
        val filePath: String?
        if (mCurrentFolder == 0) {
            // 如果用户现在看的是【所有图片】这个总相册 , 那就把拍的照片，保存到系统默认的公共相册目录（DCIM/Camera）
            filePath = randomJPGPath()
        } else {
            // 如果用户当前正在看某个具体文件夹（比如微信相册）那就把拍的照片，保存到和这个文件夹同一个目录里，让照片直接出现在当前文件夹
            val file = File(mAlbumFolders[mCurrentFolder].albumFiles[0].path.orEmpty())
            filePath = randomJPGPath(file.getParentFile())
        }
        Album.camera(this)
            .image()
            .filePath(filePath)
            .onResult(mCameraAction)
            .start()
    }

    /**
     * 录像
     */
    private fun takeVideo() {
        val filePath: String?
        if (mCurrentFolder == 0) {
            filePath = randomMP4Path()
        } else {
            val file = File(mAlbumFolders[mCurrentFolder].albumFiles[0].path.orEmpty())
            filePath = randomMP4Path(file.getParentFile())
        }
        Album.camera(this)
            .video()
            .filePath(filePath)
            .quality(mQuality)
            .limitDuration(mLimitDuration)
            .limitBytes(mLimitBytes)
            .onResult(mCameraAction)
            .start()
    }

    /**
     * 显示某个文件夹下的图片
     */
    private fun showFolderAlbumFiles(position: Int) {
        mCurrentFolder = position
        val albumFolder = mAlbumFolders[position]
        mView.bindAlbumFolder(albumFolder)
    }

    /**
     * 返回
     */
    private fun callbackResult() {
        sResult?.onAction(mCheckedList)
        dismissLoadingDialog()
        finish()
    }

    /**
     * 取消
     */
    private fun callbackCancel() {
        sCancel?.onAction("User canceled.")
        dismissLoadingDialog()
        finish()
    }

    /**
     * 加载/隐藏对话框
     */
    private fun showLoadingDialog() {
        if (!mLoadingDialog.isShowing) {
            mLoadingDialog.show()
        }
    }

    private fun dismissLoadingDialog() {
        if (mLoadingDialog.isShowing) {
            mLoadingDialog.dismiss()
        }
    }

    /**
     * 空页面拍照返回
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CODE_ACTIVITY_NULL) {
            if (resultCode == RESULT_OK) {
                val imagePath = NullActivity.parsePath(data)
                val mimeType = getMimeType(imagePath)
                if (mimeType.isNotEmpty()) {
                    mCameraAction.onAction(imagePath)
                }
            } else {
                callbackCancel()
            }
        }
    }

    /**
     * 销毁
     */
    override fun finish() {
        sSizeFilter = null
        sMimeFilter = null
        sDurationFilter = null
        sResult = null
        sCancel = null
        super.finish()
        overridePendingTransition(R.anim.set_alpha_in, R.anim.set_alpha_out)
    }

}