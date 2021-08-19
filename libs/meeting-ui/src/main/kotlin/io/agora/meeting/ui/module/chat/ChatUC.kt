package io.agora.meeting.ui.module.chat

import android.app.Activity
import android.text.TextUtils
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import io.agora.meeting.context.bean.ChatMessage
import io.agora.meeting.context.bean.MessageSendState
import io.agora.meeting.ui.R
import io.agora.meeting.ui.base.BaseUiController
import io.agora.meeting.ui.base.BindingViewHolder
import io.agora.meeting.ui.databinding.MessageChatItemBinding
import io.agora.meeting.ui.databinding.MessageChatLayoutBinding
import io.agora.meeting.ui.util.KeyboardUtil
import io.agora.meeting.ui.util.KeyboardUtil.OnKeyboardVisibleChangeListener
import kotlin.math.max

class ChatUC : BaseUiController<MessageChatLayoutBinding, ChatVM>(
        MessageChatLayoutBinding::class.java,
        ChatVM::class.java
) {

    private var adapter: ChatAdapter? = null
    private var mLayoutManager: LinearLayoutManager? = null
    private val scrollToBottomRun = Runnable {
        mLayoutManager?.scrollToPositionWithOffset(max(0, adapter!!.itemCount - 1), 0)
    }


    override fun onViewCreated() {
        super.onViewCreated()

        requireBinding().apply {
            touchOutside.setOnClickListener { KeyboardUtil.hideInput(requireContext() as Activity) }

            mLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            list.layoutManager = mLayoutManager
            list.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
                mLayoutManager?.scrollToPositionWithOffset(adapter!!.itemCount - 1, 0)
            }
            adapter = ChatAdapter()
            list.adapter = adapter

            btnSend.setOnClickListener {
                val content = etMsg.text.toString()
                if (!TextUtils.isEmpty(content)) {
                    viewModel?.sendMsg(content)
                    etMsg.text = null
                }
            }

            KeyboardUtil.listenKeyboardChange(requireLifecycleOwner(), root,
                    OnKeyboardVisibleChangeListener { visible: Boolean ->
                        touchOutside.isVisible = visible
                    })
        }

    }

    override fun onViewModelBind() {
        super.onViewModelBind()
        requireViewModel().chatMsgList.observe(requireLifecycleOwner()) {
            adapter?.submitList(it, scrollToBottomRun)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mLayoutManager = null
        adapter = null
    }

    inner class ChatAdapter : ListAdapter<ChatVM.ChatMessageWrap, BindingViewHolder<MessageChatItemBinding>>(
            object : DiffUtil.ItemCallback<ChatVM.ChatMessageWrap>() {
                override fun areItemsTheSame(oldItem: ChatVM.ChatMessageWrap, newItem: ChatVM.ChatMessageWrap) =
                        oldItem.message.messageId == newItem.message.messageId

                override fun areContentsTheSame(oldItem: ChatVM.ChatMessageWrap, newItem: ChatVM.ChatMessageWrap) =
                        oldItem.message.sendState == newItem.message.sendState
            }
    ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                BindingViewHolder(MessageChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun onBindViewHolder(holder: BindingViewHolder<MessageChatItemBinding>, position: Int) {
            val message = getItem(position)
            holder.binding.message = message.message
            holder.binding.executePendingBindings()

            if (message.message.sendState == MessageSendState.Sending) {
                holder.binding.ivSending.startAnimation(AnimationUtils.loadAnimation(holder.binding.root.context, R.anim.loading))
            } else {
                holder.binding.ivSending.clearAnimation()
            }

            holder.binding.ivFailed.setOnClickListener {
                requireViewModel().reSentMsg(message.message.messageId)
            }

            holder.binding.tvTime.isVisible = message.showTime
            if (message.showTime) {
                holder.binding.tvTime.text = DateUtils.formatDateTime(holder.binding.root.context, message.message.timestamp, DateUtils.FORMAT_SHOW_TIME)
            }
        }

    }

    override fun onVisibleToUser(visible: Boolean) {
        super.onVisibleToUser(visible)
        if (!visible) {
            KeyboardUtil.hideInput(requireContext() as Activity)
        }
    }
}