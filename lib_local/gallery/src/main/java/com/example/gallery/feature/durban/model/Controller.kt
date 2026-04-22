package com.example.gallery.feature.durban.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 裁剪控制器的配置类
 * 作用：配置裁剪界面【是否启用旋转、缩放、对应文字显示】等开关
 * 实现 Parcelable ：可以在 Activity / Fragment 之间传递对象
 */
@Parcelize
data class Controller(
    // 总开关：是否启用整个控制器
    val enable: Boolean,
    // 旋转功能开关
    val rotation: Boolean,
    // 旋转文字标题开关
    val rotationTitle: Boolean,
    // 缩放功能开关
    val scale: Boolean,
    // 缩放文字标题开关
    val scaleTitle: Boolean
) : Parcelable {

    companion object {
        @JvmStatic
        fun newBuilder(): Builder {
            return Builder()
        }
    }

    constructor(builder: Builder) : this(
        builder.enable,
        builder.rotation,
        builder.rotationTitle,
        builder.scale,
        builder.scaleTitle
    )

    class Builder {
        // 默认全部开启
        var enable = true
        var rotation = true
        var rotationTitle = true
        var scale = true
        var scaleTitle = true

        /**
         * 总开关
         */
        fun enable(enable: Boolean): Builder {
            this.enable = enable
            return this
        }

        /**
         * 旋转开关
         */
        fun rotation(rotation: Boolean): Builder {
            this.rotation = rotation
            return this
        }

        /**
         * 旋转标题开关
         */
        fun rotationTitle(rotationTitle: Boolean): Builder {
            this.rotationTitle = rotationTitle
            return this
        }

        /**
         * 缩放开关
         */
        fun scale(scale: Boolean): Builder {
            this.scale = scale
            return this
        }

        /**
         * 缩放标题开关
         */
        fun scaleTitle(scaleTitle: Boolean): Builder {
            this.scaleTitle = scaleTitle
            return this
        }

        /**
         * 构建最终 Controller 对象
         */
        fun build(): Controller {
            return Controller(this)
        }
    }

}