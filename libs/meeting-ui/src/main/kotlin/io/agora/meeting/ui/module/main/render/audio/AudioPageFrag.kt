package io.agora.meeting.ui.module.main.render.audio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import io.agora.meeting.context.bean.RenderInfo
import io.agora.meeting.ui.base.BindingViewHolder
import io.agora.meeting.ui.base.KBaseFragment
import io.agora.meeting.ui.databinding.MainRenderAudioItemBinding
import io.agora.meeting.ui.databinding.MainRenderAudioPageBinding
import io.agora.meeting.ui.util.AvatarUtil
import io.agora.meeting.ui.util.UIUtil
import kotlin.math.ceil
import kotlin.math.min

class AudioPageFrag(
        private val index: Int,
        private val viewModel: AudioLayoutVM
) : KBaseFragment() {

    companion object {

        const val PAGE_MAX_COLUMN = 3
        const val PAGE_MAX_ROW = 4
    }

    private var viewBinding: MainRenderAudioPageBinding? = null
    private var adapter: AudioLayoutAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = MainRenderAudioPageBinding.inflate(inflater, container, false)
        return viewBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateGridLayout(PAGE_MAX_COLUMN * PAGE_MAX_ROW)
        adapter = AudioLayoutAdapter()
        viewBinding?.recyclerView?.adapter = adapter

        UIUtil.closeRVtAnimator(viewBinding?.recyclerView)

        viewModel.renderInfoList.observe(viewLifecycleOwner) {
            if (isSelected() || adapter?.itemCount == 0) {
                updateListLayout(it)
            }
        }
    }

    override fun onSelectedChanged(selected: Boolean) {
        super.onSelectedChanged(selected)
        if (selected) {
            updateListLayout(viewModel.renderInfoList.value)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
        adapter = null
    }

    private fun updateListLayout(list: List<RenderInfo>?) {
        list ?: return
        val _adapter = adapter ?: return

        val fromIndex = index * PAGE_MAX_ROW * PAGE_MAX_COLUMN
        val toIndex = min(list.size, (index + 1) * PAGE_MAX_ROW * PAGE_MAX_COLUMN)
        if (fromIndex < list.size && fromIndex < toIndex) {
            val subList = list.subList(fromIndex, toIndex)
            updateGridLayout(subList.size)
            _adapter.submitList(subList)
        }
    }

    private fun calculateRow(total: Int) = ceil(total * 1.0 / PAGE_MAX_COLUMN).toInt()

    private fun updateGridLayout(total: Int) {
        val recyclerView = viewBinding?.recyclerView ?: return
        val row = calculateRow(total)
        val column = PAGE_MAX_COLUMN * 2

        val layoutManager = GridLayoutManager(requireContext(), column)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val pRow = position / (column / 2)
                if (pRow == row - 1) {
                    val leftCount = total - pRow * column / 2
                    return if (leftCount == 0) {
                        column / 3
                    } else column / leftCount
                }
                return column / 3
            }
        }
        recyclerView.layoutManager = layoutManager
    }


    inner class AudioLayoutAdapter : ListAdapter<RenderInfo, BindingViewHolder<MainRenderAudioItemBinding>>(
            RenderInfoDiffCallback()
    ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                BindingViewHolder(MainRenderAudioItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun onBindViewHolder(holder: BindingViewHolder<MainRenderAudioItemBinding>, position: Int) {
            val item = getItem(position)
            holder.binding.info = item
            holder.binding.executePendingBindings()
            // 设置头像
            AvatarUtil.loadCircleAvatar(holder.binding.root, holder.binding.ivAvatar, item.userInfo.userName)
        }
    }


    class RenderInfoDiffCallback : DiffUtil.ItemCallback<RenderInfo>() {
        override fun areItemsTheSame(oldItem: RenderInfo, newItem: RenderInfo) =
                oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: RenderInfo, newItem: RenderInfo) =
                oldItem.userInfo.userRole == newItem.userInfo.userRole
                        && oldItem.hasVideo == newItem.hasVideo
                        && oldItem.hasAudio == newItem.hasAudio
                        && oldItem.options == newItem.options

        override fun getChangePayload(oldItem: RenderInfo, newItem: RenderInfo): Any? {
            return false
        }

    }


}