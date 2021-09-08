package com.example.common.widget.advertising.adapter

import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.common.imageloader.ImageLoader.Companion.instance

/**
 *  Created by wangyanbin
 *  广告适配器
 */
class AdvertisingAdapter(var list: MutableList<String>) : RecyclerView.Adapter<AdvertisingAdapter.ViewHolder>() {
    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ImageView(parent.context))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.setOnClickListener { onItemClickListener?.onItemClick(position % list.size) }
        instance.displayImage((holder.itemView as ImageView), list[position % list.size])
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

    fun setData(list: MutableList<String>) {
        this.list = list
        notifyDataSetChanged()
    }

    interface OnItemClickListener {

        fun onItemClick(position: Int)

    }

}
