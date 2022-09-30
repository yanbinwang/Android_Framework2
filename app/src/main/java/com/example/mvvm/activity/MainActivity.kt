package com.example.mvvm.activity

import android.annotation.SuppressLint
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import androidx.appcompat.content.res.AppCompatResources
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.base.utils.function.view.click
import com.example.base.utils.function.view.focus
import com.example.base.utils.function.view.openDecor
import com.example.common.base.BaseTitleActivity
import com.example.common.constant.ARouterPath
import com.example.mvvm.R
import com.example.mvvm.databinding.ActivityMainBinding


/**
 * object是单例，适合做一些重复性的操作
 */
@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseTitleActivity<ActivityMainBinding>() {

    override fun initView() {
        super.initView()
        titleBuilder.setTitle("10086").getDefault()
        binding.edt.openDecor()
        binding.edt.focus()
    }

    @SuppressLint("SetTextI18n")
    override fun initEvent() {
        super.initEvent()
        binding.btnCopy.click {
            val drawable = AppCompatResources.getDrawable(this@MainActivity, R.mipmap.ic_launcher)
            drawable?.setBounds(0, 0, 40, 40)
            val builder = SpannableStringBuilder("Hello World!")
//            builder.setSpan(AtUserSpan(drawable!!), 3, 9, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)


            val d = AppCompatResources.getDrawable(this@MainActivity, R.mipmap.ic_launcher)
            d?.setBounds(0, 0, d.intrinsicWidth, d.intrinsicHeight)
            val span = ImageSpan(d!!, ImageSpan.ALIGN_BASELINE)
            builder.setSpan(span, 2, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)






            binding.edt.setText(builder)


//            val span = AtUserSpan(ContextCompat.getColor(this, R.color.blue_2e60df))
//            span.name = "老王"
//            binding.edt.parseAtUser(span)
        }
    }

//    fun getTextDraw(text: String, height: Int, width: Int): Drawable {
//        val newBitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        val canvas = Canvas(newBitmap);
//        canvas.drawColor(Color.BLACK)
//        canvas.drawBitmap(newBitmap, 0f, 0f, null)
//        val textPaint = TextPaint()
//        textPaint.isAntiAlias = true
//        textPaint.textSize = (height * 2 / 3).toSafeFloat()
//        textPaint.color = Color.BLACK;
//        //在Android开发中，Canvas.drawText不会换行，即使一个很长的字符串也只会显示一行，超出部分会隐藏在屏幕之外.StaticLayout是android中处理文字的一个工具类，StaticLayout 处理了文字换行的问题";
//        val sl = StaticLayout(
//            text,
//            textPaint,
//            newBitmap.width,
//            Layout.Alignment.ALIGN_NORMAL,
//            1.0f,
//            0.0f,
//            false
//        )
//        textPaint.style = Paint.Style.FILL;
//        canvas.translate(0f, (height / 10).toSafeFloat())
//        sl.draw(canvas)
//        return BitmapDrawable(newBitmap)
//    }


}