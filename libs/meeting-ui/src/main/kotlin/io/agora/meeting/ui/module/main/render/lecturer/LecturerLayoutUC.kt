package io.agora.meeting.ui.module.main.render.lecturer

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import io.agora.meeting.context.VideoTarget
import io.agora.meeting.context.VideoTargetAttachListener
import io.agora.meeting.context.bean.RenderInfo
import io.agora.meeting.context.bean.RenderInfoType
import io.agora.meeting.context.bean.RenderMode
import io.agora.meeting.ui.R
import io.agora.meeting.ui.base.BaseUiController
import io.agora.meeting.ui.base.BindingViewHolder
import io.agora.meeting.ui.databinding.MainRenderLecturerContainerBinding
import io.agora.meeting.ui.databinding.MainRenderLecturerItemBinding
import io.agora.meeting.ui.databinding.MainRenderStatsLayoutBinding
import io.agora.meeting.ui.util.AvatarUtil
import io.agora.meeting.ui.widget.WhiteBoardWrapView
import io.agora.meeting.ui.widget.gesture.GestureLayer
import io.agora.meeting.ui.widget.gesture.touch.adapter.GestureVideoTouchAdapterImpl
import io.agora.whiteboard.netless.widget.WhiteBoardView

class LecturerLayoutUC : BaseUiController<MainRenderLecturerContainerBinding, LecturerLayoutVM>(
        MainRenderLecturerContainerBinding::class.java,
        LecturerLayoutVM::class.java
) {
    private var adapter: LecturerListAdapter? = null
    private var lecturerUpdateRun : Runnable? = null

    var boardClickListener: ((View) -> Unit)? = null
    var tiledLayoutClickListener: ((View) -> Unit)? = null

    // true: long click of video will display the stats info layout
    // false: hide the stats info layout
    var statsDisplayEnable = false
        set(value) {
            val oldField = field
            if (!value && oldField) {
                viewModel?.disableAllStats()
                updateStatsLayout(null, false)
            }
            field = value
            if (value && !oldField) {
                updateLecturerStats()
            }
        }

    // special the main render id to display in full layout
    var mainRenderId: Int = -1
        set(value) {
            field = value
            viewModel?.mainRenderId = value
        }
        get() {
            val renderId = viewModel?.mainRenderId ?: return field
            if (renderId == -1) {
                return field
            }
            return renderId
        }


    override fun onViewCreated() {
        super.onViewCreated()
        requireBinding().apply {
            val offset = requireContext().resources.getDimension(R.dimen.meeting_speaker_video_offset).toInt()
            list.addItemDecoration(object : ItemDecoration() {
                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                    outRect.set(offset, offset, offset, offset)
                }
            })
            list.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            //list.layoutManager = object: LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false){
            //    override fun onLayoutCompleted(state: RecyclerView.State?) {
            //        super.onLayoutCompleted(state)
            //        unSubscriptInVisibleVideo()
            //    }
            //}
            adapter = LecturerListAdapter()
            list.adapter = adapter

            list.addOnScrollListener(object: RecyclerView.OnScrollListener(){
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        unSubscriptInVisibleVideo()
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dx == 0 && dy == 0) {
                        unSubscriptInVisibleVideo()
                    }
                }
            })

            ivTiledSwitch.setOnClickListener { v -> tiledLayoutClickListener?.invoke(v) }

            tvEnterWhiteboard.setOnClickListener { v ->
                // 跳转到白板界面
                boardClickListener?.invoke(v)
            }
            tvStopScreen.setOnClickListener { v ->
                AlertDialog.Builder(v.context)
                        .setMessage(R.string.notify_popup_leave_with_screenshare_on)
                        .setNegativeButton(R.string.cmm_no) { dialog, _ -> dialog.dismiss() }
                        .setPositiveButton(R.string.cmm_yes) { dialog, _ -> run { viewModel?.stopScreenShare() } }
                        .show()
            }
        }
    }

    private fun unSubscriptInVisibleVideo(){
        val recyclerView = binding?.list ?: return
        val itemCount = adapter?.itemCount ?: return
        val manager = recyclerView.layoutManager
        if (manager is LinearLayoutManager) {
            val firstPosition = manager.findFirstVisibleItemPosition()
            val lastPosition = manager.findLastVisibleItemPosition()
            val visibleRange = mutableListOf<Int>()
            for (i in firstPosition..lastPosition) {
                val view = manager.findViewByPosition(i) ?: continue
                val rect = Rect()
                val isVisible = view.getGlobalVisibleRect(rect)
                if (isVisible) {
                    visibleRange.add(i)
                }
            }

            for(i in 0..itemCount){
                val item = adapter?.currentList?.getOrNull(i) ?: continue
                if(!visibleRange.contains(i)){
                    viewModel?.unSubscriptVideo(item.streamId)
                }else{
                    val view = manager.findViewByPosition(i) ?: continue
                    bindMediaStream(item, view.findViewById(R.id.fl_video))
                }
            }
        }
    }

    override fun onViewModelBind() {
        super.onViewModelBind()
        requireViewModel().mainRenderId = mainRenderId
        requireViewModel().mainRender.observe(requireLifecycleOwner()) { lecturerInfo ->

            updateLecturerStats()

            requireBinding().speaker = lecturerInfo
            requireBinding().executePendingBindings()

            requireBinding().flVideo.removeCallbacks(lecturerUpdateRun)
            lecturerUpdateRun = Runnable {
                binding ?: return@Runnable
                // 显示视频
                when (lecturerInfo.type) {
                    RenderInfoType.Board -> bindBoardStream(lecturerInfo, requireBinding().flVideo)
                    RenderInfoType.ScreenSharing -> if (lecturerInfo.isMe) bindLocalScreenStream(requireBinding().flVideo) else bindScaleMediaStream(lecturerInfo, requireBinding().flVideo)
                    RenderInfoType.Media -> bindScaleMediaStream(lecturerInfo, requireBinding().flVideo)
                }
            }


            // 头像
            AvatarUtil.loadCircleAvatar(requireBinding().root, requireBinding().ivAvatar, lecturerInfo.userInfo.userName)

            requireBinding().ivTiledSwitch.isVisible = !requireViewModel().isSharing()

            // 显示统计数据
            requireBinding().llLecturerInfo.setOnLongClickListener {
                if (!statsDisplayEnable) return@setOnLongClickListener false

                if (requireViewModel().statsRenderId != lecturerInfo.id) {
                    requireViewModel().statsRenderId = lecturerInfo.id
                    requireViewModel().enableStats(lecturerInfo.id, true)
                } else {
                    val enable = !requireViewModel().isStatsEnable(lecturerInfo.id)
                    requireViewModel().enableStats(lecturerInfo.id, enable)
                    updateStatsLayout(lecturerInfo, enable)
                }
                true
            }

        }
        requireViewModel().subRenderList.observe(requireLifecycleOwner()) { list ->
            requireBinding().ivTiledSwitch.isVisible = !requireViewModel().isSharing()
            adapter?.submitList(list){
                binding?.flVideo?.postDelayed(lecturerUpdateRun, 500)
            }
        }

        requireViewModel().statsRenderInfo.observe(requireLifecycleOwner()) {
            updateStatsLayout(it)
        }
    }

    override fun onDestroy() {
        requireBinding().flVideo.removeCallbacks(lecturerUpdateRun)
        lecturerUpdateRun = null
        super.onDestroy()
    }

    private fun updateLecturerStats() {
        if (!statsDisplayEnable) return
        viewModel ?: return
        binding ?: return

        val lecturerInfo = requireViewModel().mainRender.value ?: return
        val mainRenderChange = requireBinding().speaker?.id != lecturerInfo.id
        if (requireViewModel().statsRenderId == lecturerInfo.id) {
            updateStatsLayout(lecturerInfo)
        } else if (mainRenderChange) {
            requireViewModel().statsRenderId = lecturerInfo.id
            updateStatsLayout(lecturerInfo)
        }
    }

    private fun updateStatsLayout(renderInfo: RenderInfo?,
                                  layout: Boolean? = null
    ) {
        if (!statsDisplayEnable) return
        viewModel ?: return
        binding ?: return

        val isStatsEnable = layout
                ?: if (renderInfo != null) requireViewModel().isStatsEnable(renderInfo.id) else false
        val container = binding?.statsContainer ?: return
        val statsLayout = container.getChildAt(0)
        if (statsLayout == null && isStatsEnable) {
            val inflate = MainRenderStatsLayoutBinding.inflate(LayoutInflater.from(container.context), container, true)
            inflate.info = renderInfo
            inflate.executePendingBindings()
        } else if (isStatsEnable) {
            val statsLayoutBinding = DataBindingUtil.findBinding<MainRenderStatsLayoutBinding>(statsLayout)
            if (requireViewModel().statsRenderId == renderInfo?.id) {
                statsLayoutBinding?.info = renderInfo
                statsLayoutBinding?.executePendingBindings()
            }
        } else if (statsLayout != null) {
            container.removeView(statsLayout)
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
                        && oldItem.statsInfo?.updateTime == newItem.statsInfo?.updateTime

        override fun getChangePayload(oldItem: RenderInfo, newItem: RenderInfo): Any? {
            return false
        }
    }

    inner class LecturerListAdapter : ListAdapter<RenderInfo, BindingViewHolder<MainRenderLecturerItemBinding>>(RenderInfoDiffCallback()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                BindingViewHolder(MainRenderLecturerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun onBindViewHolder(holder: BindingViewHolder<MainRenderLecturerItemBinding>, position: Int) {
            val item = getItem(position)
            holder.binding.renderInfo = item
            holder.binding.executePendingBindings()
            // 头像
            AvatarUtil.loadCircleAvatar(holder.binding.root, holder.binding.ivAvatar, item.userInfo.userName)

            // 显示视频
            when (item.type) {
                RenderInfoType.Board -> bindBoardStream(item, holder.binding.flVideo)
                RenderInfoType.ScreenSharing -> if (item.isMe) bindLocalScreenStream(holder.binding.flVideo) else bindMediaStream(item, holder.binding.flVideo)
                RenderInfoType.Media -> bindMediaStream(item, holder.binding.flVideo)
            }

            // 点击事件
            val child = holder.binding.flVideo.getChildAt(0) ?: holder.binding.flVideo
            child.setOnClickListener { v: View? ->
                if (item.hasVideo) {
                    requireViewModel().mainRenderId = item.id
                }
            }

            child.setOnLongClickListener {
                if (!statsDisplayEnable) return@setOnLongClickListener false
                if (requireViewModel().statsRenderId != item.id) {
                    requireViewModel().statsRenderId = item.id
                    requireViewModel().enableStats(item.id, true)
                } else {
                    val enable = !requireViewModel().isStatsEnable(item.id)
                    requireViewModel().enableStats(item.id, enable)
                    updateStatsLayout(item, enable)
                }
                true
            }
        }

    }

    private fun bindBoardStream(renderInfo: RenderInfo, container: ViewGroup) {
        if (renderInfo.type != RenderInfoType.Board) return
        requireViewModel().setBoardWritable(false)
        container.tag = null
        var wrapView: WhiteBoardWrapView? = null
        val child = container.getChildAt(0)
        if (child is WhiteBoardWrapView) {
            wrapView = child
            if (!wrapView.hasBoardView()) {
                wrapView.startup(requireViewModel().getWhiteBoardView() as WhiteBoardView?)
            }
        } else {
            wrapView = WhiteBoardWrapView(container.context)
            wrapView.startup(requireViewModel().getWhiteBoardView() as WhiteBoardView?)
            container.removeAllViews()
            container.addView(wrapView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    private fun bindLocalScreenStream(container: ViewGroup) {
        container.tag = null
        val child = container.getChildAt(0)
        if (child == null || child.id != R.id.local_screen_layout) {
            container.removeAllViews()
            LayoutInflater.from(container.context).inflate(R.layout.main_render_local_screen, container)
        }
    }

    private fun bindScaleMediaStream(renderInfo: RenderInfo, container: ViewGroup) {
        val streamId = renderInfo.streamId
        if (renderInfo.hasVideo) {
            val target = (container.tag as? VideoTarget)?: requireViewModel().createVideoTarget(container,
                            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            container.tag = target
            target.attachListener = object: VideoTargetAttachListener {
                override fun onRenderViewAttached(view: Any) {
                    if (view !is TextureView) return
                    if( view.parent?.parent === container) return

                    (view.parent as? ViewGroup)?.removeView(view)
                    container.removeAllViews()

                    val gestureLayer = GestureLayer(container.context, object : GestureVideoTouchAdapterImpl(view) {
                        override fun isFullScreen(): Boolean {
                            return true
                        }
                    })

                    // add gestureLayer to container
                    container.addView(gestureLayer.container, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

                    gestureLayer.container.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                }

                override fun onRenderViewDetached() {

                }
            }

            requireViewModel().subscriptVideo(renderInfo.userInfo.userId, streamId,
                    target,
                    true,
                    RenderMode.FIT)
        } else {
            requireViewModel().unSubscriptVideo(streamId)
            container.tag = null
        }
    }

    private fun bindMediaStream(renderInfo: RenderInfo, container: ViewGroup, onAttached: (()->Unit)? = null, onDetached: (()->Unit)? = null) {
        val streamId = renderInfo.streamId
        if (renderInfo.hasVideo) {
            val target = (container.tag as? VideoTarget)?:requireViewModel().createVideoTarget(container,
                    ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            container.tag = target
            target.attachListener = object: VideoTargetAttachListener{
                override fun onRenderViewAttached(view: Any) {
                    onAttached?.invoke()
                }

                override fun onRenderViewDetached() {
                    onDetached?.invoke()
                }
            }

            requireViewModel().subscriptVideo(
                    renderInfo.userInfo.userId,
                    streamId,
                    target,
                    false,
                    RenderMode.HIDDEN)
        } else {
            (container.tag as? VideoTarget)?.release()
            container.tag = null
        }
    }
}