package io.agora.meeting.ui.module.notify

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import io.agora.meeting.context.bean.NotifyMessage
import io.agora.meeting.context.bean.NotifyMessageType
import io.agora.meeting.ui.R
import io.agora.meeting.ui.base.BaseUiController
import io.agora.meeting.ui.base.BindingViewHolder
import io.agora.meeting.ui.databinding.MessageNotifyItemBinding
import io.agora.meeting.ui.databinding.MessageNotifyLayoutBinding
import io.agora.meeting.ui.util.TimeUtil

class NotifyUC : BaseUiController<MessageNotifyLayoutBinding, NotifyVM>(
        MessageNotifyLayoutBinding::class.java,
        NotifyVM::class.java
) {
    private var adapter: NotifyAdapter? = null
    private var layoutManager: LinearLayoutManager? = null
    private val scrollToBottomRun = Runnable {
        adapter?.let {
            layoutManager?.scrollToPositionWithOffset(Math.max(0, it.getItemCount() - 1), 0)
        }
    }

    var settingClickListener: ((View) -> Unit)? = null


    override fun onViewCreated() {
        super.onViewCreated()
        adapter = NotifyAdapter()
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        requireBinding().list.layoutManager = layoutManager
        requireBinding().list.adapter = adapter
    }

    override fun onViewModelBind() {
        super.onViewModelBind()
        requireViewModel().notifyMsgList.observe(requireLifecycleOwner()) {
            adapter?.submitList(it, scrollToBottomRun)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        layoutManager = null
        adapter = null
    }


    inner class NotifyAdapter : ListAdapter<NotifyMessage, BindingViewHolder<MessageNotifyItemBinding>>(
            object : DiffUtil.ItemCallback<NotifyMessage>() {
                override fun areItemsTheSame(oldItem: NotifyMessage, newItem: NotifyMessage) =
                        oldItem.messageId == newItem.messageId

                override fun areContentsTheSame(oldItem: NotifyMessage, newItem: NotifyMessage) =
                        oldItem.sender == newItem.sender
                                && oldItem.isActive == newItem.isActive

            }
    ) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                BindingViewHolder(MessageNotifyItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun onBindViewHolder(holder: BindingViewHolder<MessageNotifyItemBinding>, position: Int) {
            val message = getItem(position)
            val resources = holder.binding.root.context.resources
            val contentStr = when (message.type) {
                NotifyMessageType.ADMIN_MUTE_ALL_MIC -> resources.getString(R.string.notify_toast_mute_all_mic)
                NotifyMessageType.ADMIN_MUTE_ALL_CAM -> resources.getString(R.string.notify_toast_mute_all_cam)
                NotifyMessageType.ACCESS_CHANGE_CAM_ON -> resources.getString(R.string.notify_toast_cam_permission_off)
                NotifyMessageType.ACCESS_CHANGE_MIC_ON -> resources.getString(R.string.notify_toast_mic_permission_off)
                NotifyMessageType.ACCESS_CHANGE_CAM_OFF -> resources.getString(R.string.notify_toast_cam_permission_on)
                NotifyMessageType.ACCESS_CHANGE_MIC_OFF -> resources.getString(R.string.notify_toast_mic_permission_on)
                NotifyMessageType.ADMIN_MUTE_YOUR_CAM -> resources.getString(R.string.notify_toast_admin_turn_off_cam)
                NotifyMessageType.ADMIN_MUTE_YOUR_MIC -> resources.getString(R.string.notify_toast_admin_turn_off_mic)
                NotifyMessageType.ADMIN_CHANGE_BE_HOST -> resources.getString(R.string.notify_toast_new_admin, message.sender?.userName)
                NotifyMessageType.ADMIN_CHANGE_NO_HOST -> resources.getString(R.string.notify_toast_action_no_host)
                NotifyMessageType.USER_CHANGE_ENTER -> resources.getString(R.string.notify_toast_enter_room, message.sender?.userName)
                NotifyMessageType.USER_CHANGE_LEFT -> resources.getString(R.string.notify_toast_leave_room, message.sender?.userName)
                NotifyMessageType.SCREEN_CHANGE_ON -> resources.getString(R.string.notify_toast_screen_start, message.sender?.userName)
                NotifyMessageType.SCREEN_CHANGE_OFF -> resources.getString(R.string.notify_toast_screen_end)
                NotifyMessageType.BOARD_CHANGE_ON -> resources.getString(R.string.notify_toast_whiteboard_start, message.sender?.userName)
                NotifyMessageType.BOARD_CHANGE_OFF -> resources.getString(R.string.notify_toast_whiteboard_end, message.sender?.userName)
                NotifyMessageType.BOARD_INTERACT_ON -> resources.getString(R.string.notify_toast_whiteboard_join, message.sender?.userName)
                NotifyMessageType.USER_APPROVE_APPLY_CAM -> resources.getString(R.string.notify_popup_apply_video_title, message.sender?.userName)
                NotifyMessageType.USER_APPROVE_APPLY_MIC -> resources.getString(R.string.notify_popup_apply_audio_title, message.sender?.userName)
                NotifyMessageType.USER_APPROVE_ACCEPT_CAM -> resources.getString(R.string.notify_popup_accept_video_apply_title, message.sender?.userName)
                NotifyMessageType.USER_APPROVE_ACCEPT_MIC -> resources.getString(R.string.notify_popup_accept_audio_apply_title, message.sender?.userName)
                NotifyMessageType.NOTIFY_IN_OUT_OVER_MAX_LIMIT -> resources.getString(R.string.notify_toast_action_toast_over_max_num, message.payload as? Int)
                NotifyMessageType.NOTIFY_IN_OUT_CLOSED -> resources.getString(R.string.notify_toast_action_toast_notify_mute_always)
                NotifyMessageType.SYS_PERMISSION_MIC_DENIED -> resources.getString(R.string.notify_toast_action_mic_denied)
                NotifyMessageType.SYS_PERMISSION_CAM_DENIED -> resources.getString(R.string.notify_toast_action_cam_denied)
                NotifyMessageType.RECORD_ENDED -> resources.getString(R.string.notify_toast_record_end)
            }
            holder.binding.tvContent.text = contentStr

            // 停止倒计时
            holder.binding.cdtv.stopCount()
            holder.binding.cdtv.setOnClickListener(null)

            // 处理显示样式及点击事件
            var layoutBg = R.drawable.bg_notify_white
            var textColor = R.color.global_text_color_black
            if (message.type == NotifyMessageType.USER_APPROVE_APPLY_CAM
                    || message.type == NotifyMessageType.USER_APPROVE_APPLY_MIC
            ) {
                // 同意+倒计时
                holder.binding.cdtv.isVisible = true
                holder.binding.cdtv.setText(R.string.cmm_accept)
                holder.binding.cdtv.isEnabled = message.isActive
                if (message.isActive) {
                    requireViewModel().getInOutLimit()
                    val leftSecond: Int = when (message.type) {
                        NotifyMessageType.USER_APPROVE_APPLY_CAM ->
                            (requireViewModel().getCamApproveEffectiveSecond() - (TimeUtil.getSyncCurrentTimeMillis() - message.timestamp) / 1000).toInt()
                        else ->
                            (requireViewModel().getMicApproveEffectiveSecond() - (TimeUtil.getSyncCurrentTimeMillis() - message.timestamp) / 1000).toInt()
                    }
                    if (leftSecond > 0) {
                        holder.binding.cdtv.startCount(leftSecond)
                        holder.binding.cdtv.setOnClickListener {
                            requireViewModel().handleMsgEvent(message.messageId)
                        }
                    } else {
                        holder.binding.cdtv.isEnabled = false
                    }
                }
            } else if (message.type == NotifyMessageType.ADMIN_CHANGE_NO_HOST
            ) {
                // 成为主持人
                holder.binding.cdtv.isVisible = true
                holder.binding.cdtv.setText(R.string.main_become_host)
                holder.binding.cdtv.isEnabled = message.isActive
                if (message.isActive) {
                    holder.binding.cdtv.setOnClickListener {
                        requireViewModel().handleMsgEvent(message.messageId)
                    }
                }

            } else if (message.type == NotifyMessageType.NOTIFY_IN_OUT_OVER_MAX_LIMIT
                    || message.type == NotifyMessageType.NOTIFY_IN_OUT_CLOSED
            ) {
                // 编辑
                holder.binding.cdtv.isVisible = true
                holder.binding.cdtv.setText(R.string.cmm_edit)
                holder.binding.cdtv.isEnabled = true
                holder.binding.cdtv.setOnClickListener {
                    // 跳转设置页面
                    settingClickListener?.invoke(it)
                }
            } else if (message.type == NotifyMessageType.SYS_PERMISSION_MIC_DENIED
                    || message.type == NotifyMessageType.SYS_PERMISSION_CAM_DENIED
            ) {
                // 编辑
                holder.binding.cdtv.isVisible = true
                holder.binding.cdtv.setText(R.string.cmm_edit)
                holder.binding.cdtv.isEnabled = true
                holder.binding.cdtv.setOnClickListener {
                    // 跳转设置页面
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    intent.data = Uri.parse("package:" + holder.binding.root.context.getPackageName())
                    holder.binding.root.context.startActivity(intent)
                }
            } else {
                // 文本显示
                holder.binding.cdtv.isVisible = false
                layoutBg = R.drawable.bg_notify_black
                textColor = R.color.global_text_color_white
            }

            holder.binding.llLayout.setBackgroundResource(layoutBg)
            holder.binding.tvContent.setTextColor(resources.getColor(textColor))


            // 是否显示时间
            holder.binding.tvTime.isVisible = message.showTime
            if (message.showTime) {
                holder.binding.tvTime.text = DateUtils.formatDateTime(requireContext(), message.timestamp, DateUtils.FORMAT_SHOW_TIME)
            }
        }

    }
}