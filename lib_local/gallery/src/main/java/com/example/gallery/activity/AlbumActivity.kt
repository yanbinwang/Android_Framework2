package com.example.gallery.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.CompoundButton
import androidx.appcompat.widget.PopupMenu
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.safeGet
import com.example.gallery.R
import com.example.gallery.utils.album.Album
import com.example.gallery.utils.album.api.Contract.AlbumPresenter
import com.example.gallery.utils.album.api.Contract.AlbumView
import com.yanzhenjie.album.Action
import com.yanzhenjie.album.AlbumFile
import com.yanzhenjie.album.AlbumFolder
import com.yanzhenjie.album.Filter
import com.yanzhenjie.album.api.widget.Widget
import com.yanzhenjie.album.app.album.FolderDialog
import com.yanzhenjie.album.app.album.GalleryActivity
import com.yanzhenjie.album.app.album.data.MediaReadTask
import com.yanzhenjie.album.app.album.data.MediaReader
import com.yanzhenjie.album.app.album.data.PathConversion
import com.yanzhenjie.album.app.album.data.PathConvertTask
import com.yanzhenjie.album.app.album.data.ThumbnailBuildTask
import com.yanzhenjie.album.util.AlbumUtils
import com.yanzhenjie.album.widget.LoadingDialog
import com.yanzhenjie.mediascanner.MediaScanner
import java.io.File

/**
 * 相册选择页
 */
class AlbumActivity : BaseActivity(), AlbumPresenter, MediaReadTask.Callback, GalleryActivity.Callback, PathConvertTask.Callback, ThumbnailBuildTask.Callback {
    private var mAlbumFolders: List<AlbumFolder>? = null
    private var mCurrentFolder = 0

    private var mWidget: Widget? = null
    private var mFunction = 0
    private var mChoiceMode = 0
    private var mColumnCount = 0
    private var mHasCamera = false
    private var mLimitCount = 0

    private var mQuality = 0
    private var mLimitDuration: Long = 0
    private var mLimitBytes: Long = 0

    private var mFilterVisibility = false

    private var mCheckedList: ArrayList<AlbumFile>? = null
    private var mMediaScanner: MediaScanner? = null

    private var mView: AlbumView? = null
    private var mFolderDialog: FolderDialog? = null
    private var mCameraPopupMenu: PopupMenu? = null
    private var mLoadingDialog: LoadingDialog? = null

    private var mMediaReadTask: MediaReadTask? = null

    companion object {
        private const val CODE_ACTIVITY_NULL = 1
        var sSizeFilter: Filter<Long>? = null
        var sMimeFilter: Filter<String>? = null
        var sDurationFilter: Filter<Long>? = null
        var sResult: Action<ArrayList<AlbumFile>>? = null
        var sCancel: Action<String>? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeArgument()
        setContentView(createView())
        mView = AlbumView(this, this)
        mView?.setupViews(mWidget, mColumnCount, mHasCamera, mChoiceMode)
        mView?.setTitle(mWidget?.title)
        mView?.setCompleteDisplay(false)
        mView?.setLoadingDisplay(true)

        val checkedList = intent.getParcelableArrayListExtra<AlbumFile>(Album.KEY_INPUT_CHECKED_LIST)
        val mediaReader = MediaReader(this, sSizeFilter, sMimeFilter, sDurationFilter, mFilterVisibility)
        mMediaReadTask = MediaReadTask(mFunction, checkedList, mediaReader, this)
        mMediaReadTask?.execute()
    }

    private fun initializeArgument() {
        val argument = checkNotNull(intent.extras)
        mWidget = argument.getParcelable(Album.KEY_INPUT_WIDGET)
        mFunction = argument.getInt(Album.KEY_INPUT_FUNCTION)
        mChoiceMode = argument.getInt(Album.KEY_INPUT_CHOICE_MODE)
        mColumnCount = argument.getInt(Album.KEY_INPUT_COLUMN_COUNT)
        mHasCamera = argument.getBoolean(Album.KEY_INPUT_ALLOW_CAMERA)
        mLimitCount = argument.getInt(Album.KEY_INPUT_LIMIT_COUNT)
        mQuality = argument.getInt(Album.KEY_INPUT_CAMERA_QUALITY)
        mLimitDuration = argument.getLong(Album.KEY_INPUT_CAMERA_DURATION)
        mLimitBytes = argument.getLong(Album.KEY_INPUT_CAMERA_BYTES)
        mFilterVisibility = argument.getBoolean(Album.KEY_INPUT_FILTER_VISIBILITY)
    }

    private fun createView(): Int {
        return when (mWidget?.uiStyle) {
            Widget.STYLE_DARK -> {
                R.layout.album_activity_album_dark
            }
            Widget.STYLE_LIGHT -> {
                R.layout.album_activity_album_light
            }
            else -> {
                throw AssertionError("This should not be the case.")
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mView?.onConfigurationChanged(newConfig)
        if (mFolderDialog != null && !mFolderDialog?.isShowing.orFalse) mFolderDialog = null
    }

    override fun clickFolderSwitch() {
        if (mFolderDialog == null) {
            mFolderDialog = FolderDialog(this, mWidget, mAlbumFolders) { _, position ->
                mCurrentFolder = position
                showFolderAlbumFiles(mCurrentFolder)
            }
        }
        if (!mFolderDialog?.isShowing.orFalse) mFolderDialog?.show()
    }

    private fun showFolderAlbumFiles(position: Int) {
        this.mCurrentFolder = position
        val albumFolder = mAlbumFolders.safeGet(position)
        mView?.bindAlbumFolder(albumFolder)
    }

    override fun clickCamera(v: View?) {
        val hasCheckSize = mCheckedList?.size.orZero
        if (hasCheckSize >= mLimitCount) {
            val messageRes = when (mFunction) {
                Album.FUNCTION_CHOICE_IMAGE -> {
                    R.plurals.album_check_image_limit_camera
                }
                Album.FUNCTION_CHOICE_VIDEO -> {
                    R.plurals.album_check_video_limit_camera
                }
                Album.FUNCTION_CHOICE_ALBUM -> {
                    R.plurals.album_check_album_limit_camera
                }
                else -> {
                    throw java.lang.AssertionError("This should not be the case.")
                }
            }
            mView?.toast(resources.getQuantityString(messageRes, mLimitCount, mLimitCount))
        } else {
            when (mFunction) {
                Album.FUNCTION_CHOICE_IMAGE -> {
                    takePicture()
                }
                Album.FUNCTION_CHOICE_VIDEO -> {
                    takeVideo()
                }
                Album.FUNCTION_CHOICE_ALBUM -> {
                    if (mCameraPopupMenu == null) {
                        mCameraPopupMenu = v?.let { PopupMenu(this, it) }
                        mCameraPopupMenu?.menuInflater?.inflate(R.menu.album_menu_item_camera, mCameraPopupMenu?.menu)
                        mCameraPopupMenu?.setOnMenuItemClickListener { item ->
                            val id = item.itemId
                            if (id == R.id.album_menu_camera_image) {
                                takePicture()
                            } else if (id == R.id.album_menu_camera_video) {
                                takeVideo()
                            }
                            true
                        }
                    }
                    mCameraPopupMenu?.show()
                }
                else -> {
                    throw java.lang.AssertionError("This should not be the case.")
                }
            }
        }
    }

    private fun takePicture() {
        val filePath: String
        if (mCurrentFolder == 0) {
            filePath = AlbumUtils.randomJPGPath()
        } else {
            val file = File(mAlbumFolders.safeGet(mCurrentFolder)?.albumFiles.safeGet(0)?.path.orEmpty())
            filePath = AlbumUtils.randomJPGPath(file.parentFile)
        }
        Album.camera(this)
            .image()
            .filePath(filePath)
            .onResult(mCameraAction)
            .start()
    }

    private fun takeVideo() {
        val filePath: String
        if (mCurrentFolder == 0) {
            filePath = AlbumUtils.randomMP4Path()
        } else {
            val file = File(mAlbumFolders.safeGet(mCurrentFolder)?.albumFiles.safeGet(0)?.path.orEmpty())
            filePath = AlbumUtils.randomMP4Path(file.parentFile)
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

    private val mCameraAction = Action<String> { result ->
        if (mMediaScanner == null) {
            mMediaScanner = MediaScanner(this)
        }
        mMediaScanner?.scan(result)
        val conversion = PathConversion(sSizeFilter, sMimeFilter, sDurationFilter)
        val task = PathConvertTask(conversion, this)
        task.execute(result)
    }

    override fun tryCheckItem(button: CompoundButton?, position: Int) {
        val albumFile = mAlbumFolders.safeGet(mCurrentFolder)?.albumFiles.safeGet(position)
        if (button?.isChecked.orFalse) {
            if (mCheckedList?.size.orZero >= mLimitCount) {
                val messageRes = when (mFunction) {
                    Album.FUNCTION_CHOICE_IMAGE -> {
                        R.plurals.album_check_image_limit
                    }
                    Album.FUNCTION_CHOICE_VIDEO -> {
                        R.plurals.album_check_video_limit
                    }
                    Album.FUNCTION_CHOICE_ALBUM -> {
                        R.plurals.album_check_album_limit
                    }
                    else -> {
                        throw java.lang.AssertionError("This should not be the case.")
                    }
                }
                mView?.toast(resources.getQuantityString(messageRes, mLimitCount, mLimitCount))
                button?.isChecked = false
            } else {
                albumFile?.let {
                    it.isChecked = true
                    mCheckedList?.add(it)
                    setCheckedCount()
                }
            }
        } else {
            albumFile?.isChecked = false
            mCheckedList?.remove(albumFile)
            setCheckedCount()
        }
    }

    private fun setCheckedCount() {
        val count = mCheckedList?.size.orZero
        mView?.setCheckedCount(count)
        mView?.setSubTitle("$count/$mLimitCount")
    }

    override fun tryPreviewItem(position: Int) {
        when (mChoiceMode) {
            Album.MODE_SINGLE -> {
                val albumFile = mAlbumFolders.safeGet(mCurrentFolder)?.albumFiles.safeGet(position)
//                albumFile.setChecked(true);
//                mView.notifyItem(position);
                albumFile?.let {
                    mCheckedList?.add(it)
                    setCheckedCount()
                    callbackResult()
                }
            }
            Album.MODE_MULTIPLE -> {
                GalleryActivity.sAlbumFiles = mAlbumFolders.safeGet(mCurrentFolder)?.albumFiles
                GalleryActivity.sCheckedCount = mCheckedList?.size.orZero
                GalleryActivity.sCurrentPosition = position
                GalleryActivity.sCallback = this
                val intent = Intent(this, GalleryActivity::class.java)
                intent.putExtras(getIntent())
                startActivity(intent)
            }
            else -> {
                throw java.lang.AssertionError("This should not be the case.")
            }
        }
    }

    override fun tryPreviewChecked() {
        if (mCheckedList?.size.orZero > 0) {
            GalleryActivity.sAlbumFiles = ArrayList(mCheckedList.orEmpty())
            GalleryActivity.sCheckedCount = mCheckedList?.size.orZero
            GalleryActivity.sCurrentPosition = 0
            GalleryActivity.sCallback = this
            val intent = Intent(this, GalleryActivity::class.java)
            intent.putExtras(getIntent())
            startActivity(intent)
        }
    }

    override fun complete() {
        if (mCheckedList.isNullOrEmpty()) {
            val messageRes = when (mFunction) {
                Album.FUNCTION_CHOICE_IMAGE -> {
                    R.string.album_check_image_little
                }
                Album.FUNCTION_CHOICE_VIDEO -> {
                    R.string.album_check_video_little
                }
                Album.FUNCTION_CHOICE_ALBUM -> {
                    R.string.album_check_album_little
                }
                else -> {
                    throw AssertionError("This should not be the case.")
                }
            }
            mView?.toast(messageRes)
        } else {
            callbackResult()
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (mMediaReadTask != null) mMediaReadTask?.cancel(true)
        callbackCancel()
    }

    private fun callbackResult() {
        val task = ThumbnailBuildTask(this, mCheckedList, this)
        task.execute()
    }

    override fun onScanCallback(albumFolders: ArrayList<AlbumFolder>?, checkedFiles: ArrayList<AlbumFile>?) {
        mMediaReadTask = null
        when (mChoiceMode) {
            Album.MODE_MULTIPLE -> {
                mView?.setCompleteDisplay(true)
            }
            Album.MODE_SINGLE -> {
                mView?.setCompleteDisplay(false)
            }
            else -> {
                throw java.lang.AssertionError("This should not be the case.")
            }
        }
        mView?.setLoadingDisplay(false)
        mAlbumFolders = albumFolders
        mCheckedList = checkedFiles
        if (mAlbumFolders.safeGet(0)?.albumFiles.isNullOrEmpty()) {
            val intent = Intent(this, NullActivity::class.java)
            intent.putExtras(getIntent())
            startActivityForResult(intent, CODE_ACTIVITY_NULL)
        } else {
            showFolderAlbumFiles(0)
            val count = mCheckedList?.size.orZero
            mView?.setCheckedCount(count)
            mView?.setSubTitle("$count/$mLimitCount")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CODE_ACTIVITY_NULL -> {
                if (resultCode == RESULT_OK) {
                    val imagePath = NullActivity.parsePath(data).orEmpty()
                    val mimeType = AlbumUtils.getMimeType(imagePath)
                    if (!TextUtils.isEmpty(mimeType)) mCameraAction.onAction(imagePath)
                } else {
                    callbackCancel()
                }
            }
        }
    }

    override fun onPreviewComplete() {
        callbackResult()
    }

    override fun onPreviewChanged(albumFile: AlbumFile?) {
        val albumFiles = mAlbumFolders.safeGet(mCurrentFolder)?.albumFiles
        val position = albumFiles?.indexOf(albumFile).orZero
        val notifyPosition = if (mHasCamera) position + 1 else position
        mView?.notifyItem(notifyPosition)
        if (albumFile?.isChecked.orFalse) {
            if (!mCheckedList?.contains(albumFile).orFalse) albumFile?.let { mCheckedList?.add(it) }
        } else {
            if (mCheckedList?.contains(albumFile).orFalse) mCheckedList?.remove(albumFile)
        }
        setCheckedCount()
    }

    override fun onConvertStart() {
        showLoadingDialog()
        mLoadingDialog?.setMessage(R.string.album_converting)
    }

    override fun onConvertCallback(albumFile: AlbumFile?) {
        albumFile?.let {
            it.isChecked = !it.isDisable
            if (it.isDisable) {
                if (mFilterVisibility) addFileToList(it)
                else mView?.toast(getString(R.string.album_take_file_unavailable))
            } else {
                addFileToList(it)
            }
            dismissLoadingDialog()
        }
    }

    private fun addFileToList(albumFile: AlbumFile) {
        if (mCurrentFolder != 0) {
            val albumFiles = mAlbumFolders.safeGet(0)?.albumFiles
            if (albumFiles?.size.orZero > 0) albumFiles?.add(0, albumFile)
            else albumFiles?.add(albumFile)
        }
        val albumFolder = mAlbumFolders.safeGet(mCurrentFolder)
        val albumFiles = albumFolder?.albumFiles
        if (albumFiles.isNullOrEmpty()) {
            albumFiles?.add(albumFile)
            mView?.bindAlbumFolder(albumFolder)
        } else {
            albumFiles.add(0, albumFile)
            mView?.notifyInsertItem(if (mHasCamera) 1 else 0)
        }
        mCheckedList?.add(albumFile)
        val count = mCheckedList?.size.orZero
        mView?.setCheckedCount(count)
        mView?.setSubTitle("$count/$mLimitCount")
        when (mChoiceMode) {
            Album.MODE_SINGLE -> {
                callbackResult()
            }
            Album.MODE_MULTIPLE -> {}
            else -> {
                throw java.lang.AssertionError("This should not be the case.")
            }
        }
    }

    override fun onThumbnailStart() {
        showLoadingDialog()
        mLoadingDialog?.setMessage(R.string.album_thumbnail)
    }

    override fun onThumbnailCallback(albumFiles: ArrayList<AlbumFile>?) {
        if (sResult != null) albumFiles?.let { sResult?.onAction(it) }
        dismissLoadingDialog()
        finish()
    }

    private fun callbackCancel() {
        if (sCancel != null) sCancel?.onAction("User canceled.")
        finish()
    }

    private fun showLoadingDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = LoadingDialog(this)
            mLoadingDialog?.setupViews(mWidget)
        }
        if (!mLoadingDialog?.isShowing.orFalse) {
            mLoadingDialog?.show()
        }
    }

    /**
     * Dismiss loading dialog.
     */
    fun dismissLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog?.isShowing.orFalse) {
            mLoadingDialog?.dismiss()
        }
    }

    override fun finish() {
        sSizeFilter = null
        sMimeFilter = null
        sDurationFilter = null
        sResult = null
        sCancel = null
        super.finish()
    }

}