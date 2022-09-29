package com.example.common.widget.advertising

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
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
            local = false
            field = value
            notifyDataSetChanged()
        }
    var localList = ArrayList<Int>()
        set(value) {
            local = true
            field = value
            notifyDataSetChanged()
        }
    var onItemClickListener: OnItemClickListener? = null
    private var local: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ImageView(parent.context))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.click { onItemClickListener?.onItemClick(position % (if(local) localList.size else list.size)) }
        if (local) {
            (holder.itemView as ImageView).setBackgroundResource(localList[position % localList.size])
        } else {
            ImageLoader.instance.display((holder.itemView as ImageView), list[position % list.size])
        }
    }

    override fun getItemCount(): Int {
        return if(local) if (localList.size < 2) localList.size else Int.MAX_VALUE else if (list.size < 2) list.size else Int.MAX_VALUE
    }

    class ViewHolder(itemView: ImageView) : RecyclerView.ViewHolder(itemView) {

        init {
            //设置缩放方式
            itemView.scaleType = ImageView.ScaleType.FIT_XY
            itemView.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT)
        }
    }

    interface OnItemClickListener {

        fun onItemClick(position: Int)

    }

}
