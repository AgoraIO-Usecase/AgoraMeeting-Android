package io.agora.meeting.ui.module.main.render.tiled

import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.FrameLayout
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.agora.meeting.context.bean.RenderInfo
import io.agora.meeting.context.bean.UserOperation
import io.agora.meeting.ui.R
import io.agora.meeting.ui.base.BindingViewHolder
import io.agora.meeting.ui.base.KBaseFragment
import io.agora.meeting.ui.databinding.MainRenderStatsLayoutBinding
import io.agora.meeting.ui.databinding.MainRenderTiledItemBinding
import io.agora.meeting.ui.databinding.MainRenderTiledVpPageBinding
import io.agora.meeting.ui.util.AvatarUtil
import io.agora.meeting.ui.util.UIUtil
import kotlin.math.min

class TiledVPPageFrag(
        private val index: Int,
        private val viewModel: TiledLayoutVM,
        private val onItemClickListener: ((View?, RenderInfo) -> Unit)?
) : KBaseFragment() {

    companion object {
        private val TEXTURE_VIEW_ID = View.generateViewId()
        const val PAGE_COLUMN = 2
        const val PAGE_ROW = 2
    }

    private var viewBinding: MainRenderTiledVpPageBinding? = null
    private var adapter: TiledLayoutAdapter? = null
    private val openRvAnimRun = Runnable {
        viewBinding?.recyclerView?.let {
            UIUtil.openRVAnimator(it)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = MainRenderTiledVpPageBinding.inflate(inflater, container, false)
        return viewBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = viewBinding ?: return
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                val offset = 3
                val layoutParams = view.layoutParams
                layoutParams.width = ((parent.width - offset * 2 * PAGE_COLUMN) / PAGE_COLUMN)
                layoutParams.height = ((parent.height - offset * 2 * PAGE_ROW) / PAGE_ROW)
                view.layoutParams = layoutParams
                outRect[offset, offset, offset] = offset
            }
        })
        binding.recyclerView.layoutManager = object: GridLayoutManager(requireContext(), PAGE_ROW){
            override fun canScrollVertically(): Boolean {
                return false
            }
        }

        adapter = TiledLayoutAdapter()
        binding.recyclerView.adapter = adapter

        viewModel.renderList.observe(viewLifecycleOwner) {
            if (isSelected() || adapter?.itemCount == 0) {
                updateListAdapter(it)
            }
        }

        if (!isSelected()) {
            UIUtil.closeRVtAnimator(viewBinding?.recyclerView)
        }
    }

    private fun updateListAdapter(list: List<TiledLayoutVM.TiledRenderInfo>?, run: Runnable? = null) {
        list ?: return
        val fromIndex = index * PAGE_COLUMN * PAGE_ROW
        val toIndex = min(list.size, (index + 1) * PAGE_ROW * PAGE_COLUMN)
        if (fromIndex < list.size && fromIndex < toIndex) {
            adapter?.submitList(list.subList(fromIndex, toIndex).map {
                it.copy(isVisible = isSelected())
            }, run)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cancelOpenRvAnimator()
        viewBinding = null
        adapter = null
    }

    override fun onSelectedChanged(selected: Boolean) {
        super.onSelectedChanged(selected)
        if (selected) {
            updateListAdapter(viewModel.renderList.value, Runnable {
                postOpenRvAnimator()
            })
        } else {
            cancelOpenRvAnimator()
            UIUtil.closeRVtAnimator(viewBinding?.recyclerView)
            updateListAdapter(viewModel.renderList.value)
        }
    }

    private fun postOpenRvAnimator() {
        if (isSelected()) {
            viewBinding?.recyclerView?.removeCallbacks(openRvAnimRun)
            viewBinding?.recyclerView?.postDelayed(openRvAnimRun, 500)
        }
    }

    private fun cancelOpenRvAnimator() {
        viewBinding?.recyclerView?.removeCallbacks(openRvAnimRun)
    }


    private fun alertPopWindow(v: CheckBox, renderInfo: RenderInfo) {
        val context = v.context.applicationContext
        val optionStrList = renderInfo.options.map {
            context.resources.getString(
                    when (it) {
                        UserOperation.AbandonHost -> R.string.more_renounce_admin
                        UserOperation.BeHost -> R.string.more_become_admin
                        UserOperation.SetAsHost -> R.string.more_set_host
                        UserOperation.CloseCamera -> R.string.more_close_video
                        UserOperation.CloseMic -> R.string.more_mute_audio
                        else -> R.string.more_move_out
                    }
            )
        }


        // 弹窗
        val popupWindow = ListPopupWindow(context)
        popupWindow.width = context.resources.getDimension(R.dimen.meeting_pop_dialog_width).toInt()
        popupWindow.anchorView = v
        popupWindow.verticalOffset = context.resources.getDimension(R.dimen.meeting_pop_dialog_offset).toInt()
        popupWindow.setDropDownGravity(Gravity.END)
        popupWindow.setBackgroundDrawable(requireContext().getDrawable(R.drawable.bg_video_popup_window))
        popupWindow.setListSelector(ColorDrawable(Color.TRANSPARENT))
        popupWindow.setAdapter(object : ArrayAdapter<String?>(context,
                R.layout.layout_video_popup_item,
                android.R.id.text1,
                optionStrList) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                if (position != optionStrList.size - 1) {
                    view.findViewById<View>(R.id.divider).visibility = View.VISIBLE
                }
                return view
            }
        })
        popupWindow.setOnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
            viewModel.dealUserOperation(renderInfo, renderInfo.options[position])
            popupWindow.dismiss()
        }
        popupWindow.setOnDismissListener {
            v.isChecked = false
            v.post { v.isEnabled = true }
        }
        v.isEnabled = false
        popupWindow.show()
    }

    private fun bindMediaStream(info: TiledLayoutVM.TiledRenderInfo, container: ViewGroup) {
        val renderInfo = info.renderInfo
        container.tag = null
        if (renderInfo.hasVideo) {
            val streamId = renderInfo.streamId
            var textureView = container.findViewById<TextureView>(TEXTURE_VIEW_ID)
            if (textureView != null) {
                if (textureView.isAvailable) {
                    val tag = textureView.tag
                    if (streamId == tag) {
                        // return if the SurfaceView has bound this uid
                        if (info.isVisible) {
                            viewModel.subscriptVideo(renderInfo.userInfo.userId, renderInfo.streamId, textureView, true)
                        } else {
                            viewModel.unSubscriptVideo(renderInfo.streamId)
                        }
                        return
                    }
                }
            }
            val createTextureView = viewModel.createTextureView(container.context)
            textureView = if (createTextureView == null) null else createTextureView as TextureView
            if (textureView == null) {
                container.removeAllViews()
                return
            }
            textureView.tag = streamId // bind uid
            textureView.id = TEXTURE_VIEW_ID
            container.removeAllViews()
            container.addView(textureView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            if (info.isVisible) {
                viewModel.subscriptVideo(renderInfo.userInfo.userId, renderInfo.streamId, textureView, true)
            } else {
                viewModel.unSubscriptVideo(renderInfo.streamId)
            }
        } else {
            container.removeAllViews()
        }
    }

    private fun updateStatsLayout(container: FrameLayout, renderInfo: RenderInfo, isStatsEnable: Boolean) {
        val statsLayout = container.getChildAt(0)
        if (statsLayout == null && isStatsEnable) {
            val inflate = MainRenderStatsLayoutBinding.inflate(LayoutInflater.from(container.context), container, true)
            inflate.info = renderInfo
            inflate.executePendingBindings()
        } else if (isStatsEnable) {
            val statsLayoutBinding = DataBindingUtil.findBinding<MainRenderStatsLayoutBinding>(statsLayout)
            statsLayoutBinding?.info = renderInfo
            statsLayoutBinding?.executePendingBindings()
        } else if (statsLayout != null) {
            container.removeView(statsLayout)
        }
    }


    inner class TiledLayoutAdapter : ListAdapter<TiledLayoutVM.TiledRenderInfo, BindingViewHolder<MainRenderTiledItemBinding>>(
            TiledRenderInfoDiffCallback()
    ) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                BindingViewHolder(MainRenderTiledItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent, false))


        override fun onBindViewHolder(viewHolder: BindingViewHolder<MainRenderTiledItemBinding>, position: Int) {
            val item = getItem(position)
            if (item == null) {
                viewHolder.binding.root.isVisible = false
                return
            }
            val renderInfo = item.renderInfo
            viewHolder.binding.root.isVisible = true
            viewHolder.binding.info = renderInfo
            viewHolder.binding.executePendingBindings()

            // 绑定视频流
            bindMediaStream(item, viewHolder.binding.flVideo)

            // 设置头像
            AvatarUtil.loadCircleAvatar(viewHolder.binding.root, viewHolder.binding.ivAvatar, renderInfo.userInfo.userName)

            // 点击视频后变成演讲者视图
            val child = viewHolder.binding.flVideo.getChildAt(0)
            child?.setOnClickListener { v: View? ->
                onItemClickListener?.invoke(v, renderInfo)
            }

            // 更新统计数据
            if (viewModel.statsDisplayEnable) {
                updateStatsLayout(viewHolder.binding.statsContainer, renderInfo, viewModel.isStatsEnable(renderInfo.id))
            }
            child?.setOnLongClickListener {
                if (!viewModel.statsDisplayEnable) return@setOnLongClickListener false

                val enable = !viewModel.isStatsEnable(renderInfo.id)
                updateStatsLayout(viewHolder.binding.statsContainer, renderInfo, enable)
                viewModel.enableStats(renderInfo.id, enable)
                true
            }

            // 更多菜单显示点击
            viewHolder.binding.cbMore.isVisible = renderInfo.options.isNotEmpty()
            viewHolder.binding.cbMore.setOnClickListener { v ->
                alertPopWindow(viewHolder.binding.cbMore, renderInfo)
            }

            // 用户置顶
            viewHolder.binding.cbTop.isChecked = item.isTop
            viewHolder.binding.cbTop.setOnClickListener { btn ->
                viewModel.setTop(item.index, viewHolder.binding.cbTop.isChecked)
            }
        }
    }

    class TiledRenderInfoDiffCallback : DiffUtil.ItemCallback<TiledLayoutVM.TiledRenderInfo>() {
        override fun areItemsTheSame(oldItem: TiledLayoutVM.TiledRenderInfo, newItem: TiledLayoutVM.TiledRenderInfo) =
                oldItem.renderInfo.id == newItem.renderInfo.id

        override fun areContentsTheSame(oldItem: TiledLayoutVM.TiledRenderInfo, newItem: TiledLayoutVM.TiledRenderInfo) =
                oldItem.renderInfo.userInfo.userRole == newItem.renderInfo.userInfo.userRole
                        && oldItem.renderInfo.hasVideo == newItem.renderInfo.hasVideo
                        && oldItem.renderInfo.hasAudio == newItem.renderInfo.hasAudio
                        && oldItem.isTop == newItem.isTop
                        && oldItem.index == newItem.index
                        && oldItem.renderInfo.options == newItem.renderInfo.options
                        && oldItem.isVisible == newItem.isVisible
                        && oldItem.renderInfo.statsInfo?.updateTime == newItem.renderInfo.statsInfo?.updateTime

        override fun getChangePayload(oldItem: TiledLayoutVM.TiledRenderInfo, newItem: TiledLayoutVM.TiledRenderInfo): Any? {
            return false
        }

    }


}