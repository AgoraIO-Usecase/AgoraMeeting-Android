package io.agora.meeting.ui.base

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

class BindingViewHolder<Binding: ViewBinding>(val binding: Binding) : RecyclerView.ViewHolder(binding.root) {
}