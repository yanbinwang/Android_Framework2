package com.example.mvvm.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.visible
import com.example.framework.widget.BaseViewGroup
import com.example.mvvm.R
import com.example.mvvm.databinding.ViewFoldBinding

/**
 * @description 折疊view
 * @author yan
 */
class FoldView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseViewGroup(context, attrs, defStyleAttr) {
    private val binding by lazy { ViewFoldBinding.bind(context.inflate(R.layout.view_fold)) }
    private val animatorIn by lazy { AnimationUtils.loadAnimation(context, R.anim.top_sheet_slide_in) }

    init {
        binding.flArrow.click {
            if (binding.flFold.visibility == View.GONE) {
                binding.flFold.visible()
                binding.flFold.startAnimation(animatorIn)
                binding.ivArrow.rotation = 180f
            } else {
                binding.flFold.gone()
                binding.ivArrow.rotation = 0f
            }
        }
    }

    override fun onInflateView() {
        if (isInflate()) addView(binding.root)
    }

    fun addShow(view: View) {
        binding.flShow.removeAllViews()
        binding.flShow.addView(view)
    }

    fun addFold(view: View) {
        binding.flFold.removeAllViews()
        binding.flFold.addView(view)
    }

    fun setArrowRes(resId: Int) {
        binding.ivArrow.setImageResource(resId)
    }

    fun setBackground(resId: Int) {
        binding.llContainer.setBackgroundResource(resId)
    }

    fun getArrow() = binding.ivArrow

}