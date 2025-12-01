package com.example.common.widget.advertising

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.common.utils.function.ptFloat
import com.example.framework.utils.function.defTypeMipmap
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.safeSize
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.init
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
        return ViewHolder(CardView(parent.context).also { it.init(radius.ptFloat) })
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        (holder.itemView as? CardView)?.let {
            val index = position.mod(list.safeSize)
            val uri = list.safeGet(index) ?: return
            it.radius = radius.ptFloat
            if (localAsset) {
                ImageLoader.instance.loadCardViewFromDrawable(it, it.context.defTypeMipmap(uri))
            } else {
                ImageLoader.instance.loadCardViewFromUrl(it, uri)
            }
            it.click {
                onItemClick?.invoke(index)
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