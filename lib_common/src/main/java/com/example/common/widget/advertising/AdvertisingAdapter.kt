package com.example.common.widget.advertising

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.framework.utils.function.mipmapId
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.safeSize
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.setResource
import com.example.glide.ImageLoader

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
    var localAsset = false
    var onItemClick: ((position: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ImageView(parent.context))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.click { onItemClick?.invoke(position.mod(list.safeSize)) }
        val bean = list.safeGet(position.mod(list.safeSize)).orEmpty()
        val image = holder.itemView as? ImageView ?: return
        if (localAsset) {
            image.setResource(holder.itemView.context.mipmapId(bean))
        } else {
            ImageLoader.instance.display(image, bean)
        }
    }

    override fun getItemCount(): Int {
        return if (list.size < 2) list.safeSize else Int.MAX_VALUE
    }

    class ViewHolder(itemView: ImageView) : RecyclerView.ViewHolder(itemView) {
        init {
            //设置缩放方式
            itemView.scaleType = ImageView.ScaleType.FIT_XY
            itemView.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT)
        }
    }

}
