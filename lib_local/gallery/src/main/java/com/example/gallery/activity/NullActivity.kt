package com.example.gallery.activity

import android.content.Intent
import android.os.Bundle
import com.example.gallery.utils.album.Album
import com.example.gallery.utils.album.api.Contract
import com.yanzhenjie.album.Action
import com.yanzhenjie.album.R
import com.yanzhenjie.album.api.widget.Widget
import com.yanzhenjie.album.mvp.BaseActivity

class NullActivity : BaseActivity(), Contract.NullPresenter {
    private var mWidget: Widget? = null
    private var mQuality = 1
    private var mLimitDuration: Long = 0
    private var mLimitBytes: Long = 0
    private var mView: Contract.NullView? = null

    companion object {
        private const val KEY_OUTPUT_IMAGE_PATH = "KEY_OUTPUT_IMAGE_PATH"

        @JvmStatic
        fun parsePath(intent: Intent?): String? {
            return intent?.getStringExtra(KEY_OUTPUT_IMAGE_PATH)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.album_activity_null)
        mView = Contract.NullView(this, this)

        val argument = intent.extras
        checkNotNull(argument)
        val function = argument.getInt(Album.KEY_INPUT_FUNCTION)
        val hasCamera = argument.getBoolean(Album.KEY_INPUT_ALLOW_CAMERA)

        mQuality = argument.getInt(Album.KEY_INPUT_CAMERA_QUALITY)
        mLimitDuration = argument.getLong(Album.KEY_INPUT_CAMERA_DURATION)
        mLimitBytes = argument.getLong(Album.KEY_INPUT_CAMERA_BYTES)

        mWidget = argument.getParcelable(Album.KEY_INPUT_WIDGET)
        mView?.setupViews(mWidget)
        mView?.setTitle(mWidget?.title)

        when (function) {
            Album.FUNCTION_CHOICE_IMAGE -> {
                mView?.setMessage(R.string.album_not_found_image)
                mView?.setMakeVideoDisplay(false)
            }

            Album.FUNCTION_CHOICE_VIDEO -> {
                mView?.setMessage(R.string.album_not_found_video)
                mView?.setMakeImageDisplay(false)
            }

            Album.FUNCTION_CHOICE_ALBUM -> {
                mView?.setMessage(R.string.album_not_found_album)
            }

            else -> {
                throw AssertionError("This should not be the case.")
            }
        }

        if (!hasCamera) {
            mView?.setMakeImageDisplay(false)
            mView?.setMakeVideoDisplay(false)
        }
    }

    override fun takePicture() {
        Album.camera(this)
            .image()
            .onResult(mCameraAction)
            .start()
    }

    override fun takeVideo() {
        Album.camera(this)
            .video()
            .quality(mQuality)
            .limitDuration(mLimitDuration)
            .limitBytes(mLimitBytes)
            .onResult(mCameraAction)
            .start()
    }

    private val mCameraAction = Action<String> { result ->
        val intent = Intent()
        intent.putExtra(KEY_OUTPUT_IMAGE_PATH, result)
        setResult(RESULT_OK, intent)
        finish()
    }

}