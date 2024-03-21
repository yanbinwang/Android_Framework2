package com.example.common.widget.advertising

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.common.utils.function.pt
import com.example.framework.utils.function.defTypeMipmap
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.safeSize
import com.example.framework.utils.function.view.click
import com.example.glide.ImageLoader

/**
 *  Created by wangyanbin
 *  广告适配器
 */
@SuppressLint("NotifyDataSetChanged")
class AdvertisingAdapter : RecyclerView.Adapter<AdvertisingAdapter.ViewHolder>() {
    private var radius = 0
    private var localAsset = false
    private var list = ArrayList<String>()
    private var onItemClick: ((position: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ImageView(parent.context))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.click { onItemClick?.invoke(position.mod(list.safeSize)) }
        val bean = list.safeGet(position.mod(list.safeSize)).orEmpty()
        val image = holder.itemView as? ImageView ?: return
        if (localAsset) {
//            image.setDrawable(holder.itemView.context.defTypeMipmap(bean))
            ImageLoader.instance.displayRoundIdentifier(image, holder.itemView.context.defTypeMipmap(bean), radius = radius.pt)
        } else {
            ImageLoader.instance.displayRound(image, bean, radius = radius.pt)
        }
    }

    override fun getItemCount(): Int {
        return if (list.size < 2) list.safeSize else Int.MAX_VALUE
    }

    fun refresh(list: List<String>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    fun setParams(radius: Int = 0, localAsset: Boolean) {
        this.radius = radius
        this.localAsset = localAsset
    }

    fun setOnItemClickListener(onItemClick: ((position: Int) -> Unit)) {
        this.onItemClick = onItemClick
    }

    class ViewHolder(itemView: ImageView) : RecyclerView.ViewHolder(itemView) {
        init {
            //设置缩放方式
            itemView.scaleType = ImageView.ScaleType.FIT_XY
            itemView.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT)
        }
    }

}
