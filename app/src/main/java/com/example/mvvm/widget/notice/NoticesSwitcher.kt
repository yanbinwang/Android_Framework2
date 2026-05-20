package com.example.mvvm.widget.notice

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextSwitcher
import android.widget.TextView
import android.widget.ViewSwitcher
import androidx.lifecycle.LifecycleOwner
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.view.click
import com.example.mvvm.R

/**
 * 消息轮播
 * <com.anyixing.etc.staff.view.NoticesSwitcher
 *     android:id="@+id/notices_switcher"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"/>
 *
 * List<Notice> result = new ArrayList<>();
 * result.add(new Notice("两部门向灾区预拨2亿元救灾资金"));
 * result.add(new Notice("特朗普被裁定不具备总统参选资格"));
 * result.add(new Notice("泰国瑞幸向中国瑞幸索赔20亿元"));
 * binding.layoutNotices.setVisibility(View.VISIBLE);
 * binding.noticesSwitcher
 *         .setInAnimation(R.anim.anim_notice_in)
 *         .setOutAnimation(R.anim.anim_notice_out)
 *         .bindData(result)
 *         .startSwitch(2500);
 */
class NoticesSwitcher(context: Context, attrs: AttributeSet? = null) : TextSwitcher(context, attrs), ViewSwitcher.ViewFactory {
    private var timeInterval = 0L
    private var currentIndex = 0
    private var currentBean: Notice? = null
    private var data: List<Notice>? = null
    private var listener: ((bean: Notice?) -> Unit)? = null
    private val switchHandler by lazy { object : Handler(Looper.getMainLooper()) {
        override fun dispatchMessage(msg: Message) {
            super.dispatchMessage(msg)
            val index = currentIndex % data?.size.orZero
            currentBean = data.safeGet(index)
            setText(data.safeGet(index)?.title)
            currentIndex++
            if (data?.size.orZero > 1) {
                sendEmptyMessageDelayed(0, timeInterval)
            }
        }
    }}

    init {
        setFactory(this)
        setInAnimation(R.anim.set_translate_notice_in)
        setOutAnimation(R.anim.set_translate_notice_out)
    }

    override fun makeView(): View {
        val view = context.inflate(R.layout.item_notice_switcher)
        val tvLabel = view.findViewById<TextView>(R.id.tv_label)
        tvLabel.click { listener?.invoke(currentBean) }
        return view
    }

    /**
     * 通知/公告数据绑定
     */
    fun bindData(owner: LifecycleOwner, data: List<Notice>): NoticesSwitcher {
        owner.doOnDestroy { switchHandler.removeCallbacksAndMessages(null) }
        this.data = data
        return this
    }

    /**
     * 开启滚动/默认1秒
     */
    fun startSwitch(timeInterval: Long = 1000L) {
        this.timeInterval = timeInterval
        switchHandler.removeMessages(0)
        if (data?.size.orZero > 0) {
            switchHandler.sendEmptyMessage(0)
        } else {
            throw RuntimeException("data is empty")
        }
    }

    /**
     * 设置进入动画
     */
    fun setInAnimation(animationResId: Int): NoticesSwitcher {
        val animation = AnimationUtils.loadAnimation(context, animationResId)
        inAnimation = animation
        return this
    }

    /**
     * 设置退出动画
     */
    fun setOutAnimation(animationResId: Int): NoticesSwitcher {
        val animation = AnimationUtils.loadAnimation(context, animationResId)
        outAnimation = animation
        return this
    }

    /**
     * 设置监听
     */
    fun setOnItemClickListener(listener: ((bean: Notice?) -> Unit)) {
        this.listener = listener
    }

}