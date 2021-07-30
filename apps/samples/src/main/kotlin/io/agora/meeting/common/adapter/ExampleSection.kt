package io.agora.meeting.common.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.agora.meeting.R
import io.agora.meeting.common.model.ExampleBean
import io.github.luizgrp.sectionedrecyclerviewadapter.Section
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters

class ExampleSection(
        private val mTitle: String,
        private val mValues: MutableList<ExampleBean>?,
        private val itemClick: ItemClickListener?
) : Section(
        SectionParameters
                .builder()
                .headerResourceId(R.layout.layout_main_list_section)
                .itemResourceId(R.layout.layout_main_list_item)
                .build()
) {


    override fun getContentItemsTotal() = mValues?.size ?: 0

    override fun getItemViewHolder(view: View?) = ViewHolder(view!!)

    override fun getHeaderViewHolder(view: View?) = ViewHolder(view!!)

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is ViewHolder) {
            holder.mItem = mValues?.get(position)
            holder.mNameView.text = holder.mView.getContext().getString(holder.mItem?.nameStrId ?: 0)
            holder.mView.setOnClickListener {
                holder.mItem?.let {
                    itemClick?.onItemClick(it)
                }
            }
        }
    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder?) {
        super.onBindHeaderViewHolder(holder)
        if (holder is ViewHolder) {
            holder.mNameView.text = mTitle
        }
    }


    class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mNameView: TextView = mView.findViewById(R.id.item_name)
        var mItem: ExampleBean? = null
    }

    interface ItemClickListener {
        fun onItemClick(item: ExampleBean)
    }

}