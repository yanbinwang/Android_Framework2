package com.example.common.widget.advertising

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.base.utils.function.value.safeGet
import com.example.base.utils.function.value.toSafeInt
import com.example.base.utils.function.view.click
import com.example.common.imageloader.ImageLoader

/**
 *  Created by wangyanbin
 *  广告适配器
 */
@SuppressLint("NotifyDataSetChanged")
class AdvertisingAdapter : RecyclerView.Adapter<AdvertisingAdapter.ViewHolder>() {
    var list: MutableList<String> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var localAsset: Boolean = false
    var onItemClick: ((position: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ImageView(parent.context))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.click { onItemClick?.invoke(position % list.size) }
        if (localAsset) {
            (holder.itemView as ImageView).setBackgroundResource(list.safeGet(position % list.size).toSafeInt())
        } else {
            ImageLoader.instance.display((holder.itemView as ImageView), list.safeGet(position % list.size))
        }
    }

    override fun getItemCount(): Int {
        return if (list.size < 2) list.size else Int.MAX_VALUE
    }

    class ViewHolder(itemView: ImageView) : RecyclerView.ViewHolder(itemView) {

        init {
            //设置缩放方式
            itemView.scaleType = ImageView.ScaleType.FIT_XY
            itemView.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT)
        }
    }

}
