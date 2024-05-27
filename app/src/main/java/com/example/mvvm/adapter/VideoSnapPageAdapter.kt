package com.example.mvvm.adapter

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.safeSize

@SuppressLint("NotifyDataSetChanged")
class VideoSnapPageAdapter(mActivity: FragmentActivity) : FragmentStateAdapter(mActivity) {
    private val mData by lazy { ArrayList<Fragment>() }

    override fun getItemCount() = mData.safeSize

    override fun createFragment(position: Int): Fragment {
        return mData.safeGet(position) ?: Fragment()
    }

    fun refresh(list: List<Fragment>) {
        mData.clear()
        mData.addAll(list)
        notifyDataSetChanged()
    }

}