package com.example.common.widget.advertising

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.common.R
import com.example.common.utils.function.ptFloat
import com.example.framework.utils.function.defTypeMipmap
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.safeSize
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.size
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
        return ViewHolder(CardView(ContextThemeWrapper(parent.context, R.style.CardViewStyle)).apply {
            radius = radius.ptFloat
        })
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.apply {
            click {
                onItemClick?.invoke(position.mod(list.safeSize))
            }
            val uri = list.safeGet(position.mod(list.safeSize)) ?: return
            val image = ImageView(context)
            image.scaleType = ImageView.ScaleType.FIT_XY
            (this as? CardView)?.also {
                it.addView(image)
                it.radius = this@AdvertisingAdapter.radius.ptFloat
            }
            image.size(MATCH_PARENT,MATCH_PARENT)
            if (localAsset) {
                ImageLoader.instance.loadImageDrawableFromResource(image, context.defTypeMipmap(uri))
            } else {
                ImageLoader.instance.loadImageDrawableFromUrl(image, uri)
            }
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

    class ViewHolder(itemView: CardView) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }
    }

}
