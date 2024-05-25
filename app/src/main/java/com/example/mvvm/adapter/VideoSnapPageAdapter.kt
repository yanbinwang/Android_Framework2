package com.example.mvvm.adapter

import android.annotation.SuppressLint
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.safeSize
import com.example.mvvm.fragment.VideoSnapFragment

@SuppressLint("NotifyDataSetChanged")
class VideoSnapPageAdapter(mActivity: FragmentActivity) : FragmentStateAdapter(mActivity) {
    private val mData by lazy { ArrayList<VideoSnapFragment>() }

    override fun getItemCount() = mData.safeSize

    override fun createFragment(position: Int): VideoSnapFragment {
        return mData.safeGet(position) ?: VideoSnapFragment()
    }

    fun refresh(list: List<VideoSnapFragment>) {
        mData.clear()
        mData.addAll(list)
        notifyDataSetChanged()
    }

}