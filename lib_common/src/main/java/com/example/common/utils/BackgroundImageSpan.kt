package com.example.common.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.text.ParcelableSpan
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.ReplacementSpan
import androidx.core.content.ContextCompat
import com.example.base.utils.function.value.toSafeFloat

/**
 * @description
 * @author
 * //前两行使用这则表达式来解析出一大段文本里面需要特殊设置的文本内容
 * Matcher textMatcher;
 * SpannableString str = new SpannableString(contentStr);
 * textMatcher = PATTERN_TEXT_SRC.matcher(content);
 * while (textMatcher.find()) {
 * //然后开始循环所有的文本来进行设置。
 * final String blank = textMatcher.group().trim();
 * int index = content.indexOf(blank);
 * ClickableSpan clickableSpan = new MyClickableSpan();
 * BackgroundImageSpan backgroundColorSpan;
 * //此处之所以有这个判断，完全是为了用来设置点击时候的样式，而currentStart和currentEnd也是点击的文字的第一字符的position和最后一个字符的position
 * if (currentStart == index && currentEnd == index + blank.length()) {
 * backgroundColorSpan = new BackgroundImageSpan(R.drawable.bg_answer_wrong, getResources().getDrawable(R.drawable.bg_answer_wrong));
 * } else {
 * backgroundColorSpan = new BackgroundImageSpan(R.drawable.bg_noanswer_unselected, getResources().getDrawable(R.drawable.bg_noanswer_unselected));
 * }
 * str.setSpan(backgroundColorSpan, index, index + blank.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 * str.setSpan(clickableSpan, index, index + blank.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);}
 * //如果要设置点击事件，那么久一低昂要设置下面这一行，否则点击事件不起作用
 * mTextView.setMovementMethod(LinkMovementMethod.getInstance());
 * mTextView.setText(str);
 *
 * //此处是定义的ClickableSpan
 * private class MyClickableSpan extends ClickableSpan {
 * @Override
 * public void onClick(View widget) {
 * //这里面的代码主要是用来获取所点击的那一部分的text，也可以根据下面的方法来获取具体点击的那部分文字内容
 * TextView tv = (TextView) widget;
 * Spanned s = (Spanned) tv.getText();
 * int start = s.getSpanStart(this);
 * int end = s.getSpanEnd(this);
 * currentStart = start;
 * currentEnd = end;
 * updateBlank();
 * Toast.makeText(MainActivity.this, tv.getText(), Toast.LENGTH_SHORT).show();
 */
class BackgroundImageSpan : ReplacementSpan, ParcelableSpan {
    private var mDrawable: Drawable? = null
    private var mImageId = 0
    private var mWidth = -1

    constructor(id: Int, drawable: Drawable) {
        mImageId = id
        mDrawable = drawable
    }

    constructor(src: Parcel) {
        mImageId = src.readInt()
    }

    override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        draw(canvas, mWidth, x, top, y, bottom, paint);
        canvas.drawText(text.toString(), start, end, x, y.toSafeFloat(), paint);
    }

    override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        val size = paint.measureText(text, start, end)
        if (fm != null) paint.getFontMetricsInt(fm)
        mWidth = size.toInt()
        return mWidth
    }

    override fun updateDrawState(ds: TextPaint?) {
    }

    override fun getSpanTypeId(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(mImageId)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun convertToDrawable(context: Context) {
        if (mDrawable == null) mDrawable = ContextCompat.getDrawable(context, mImageId)
    }

    fun convert(text: CharSequence, context: Context) {
        if (text !is SpannableStringBuilder) return
        val spans = text.getSpans(0, text.length, BackgroundImageSpan::class.java)
        if (spans == null || spans.isEmpty()) return
        for (i in spans.indices) {
            spans[i].convertToDrawable(context)
        }
    }

    fun draw(canvas: Canvas, width: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint?) {
        if (mDrawable == null) return
        val drawable = mDrawable
        canvas.save()
        canvas.translate(x, top.toFloat()) // translate to the left top point
        mDrawable?.setBounds(0, 0, width, bottom - top)
        drawable?.draw(canvas)
        canvas.restore()
    }

}