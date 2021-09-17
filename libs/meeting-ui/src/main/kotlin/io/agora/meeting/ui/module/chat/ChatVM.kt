package io.agora.meeting.ui.module.chat

import androidx.lifecycle.MutableLiveData
import io.agora.meeting.context.MessagesContext
import io.agora.meeting.context.bean.ChatMessage
import io.agora.meeting.context.bean.NotifyMessage
import io.agora.meeting.context.bean.UserInfo
import io.agora.meeting.ui.base.BaseViewModel
import java.util.concurrent.Executors
import kotlin.math.ceil

class ChatVM(
        private val messagesContext: MessagesContext
) : BaseViewModel() {

    private val workerThread = Executors.newSingleThreadScheduledExecutor()

    val chatMsgList = MutableLiveData<List<ChatMessageWrap>>()

    private val messagesEventHandler = object : MessagesContext.MessagesEventHandler {

        override fun onChatMessagesUpdated(messages: List<ChatMessage>) {
            updateList()
        }

        override fun onNotifyMessagesUpdated(messages: List<NotifyMessage>) {

        }

        override fun onPrivateChatMessageReceived(content: String, fromUser: UserInfo) {

        }

    }

    init {
        messagesContext.registerEventHandler(messagesEventHandler)
        updateList()
    }

    private fun updateList() {
        workerThread.submit {
            val msgs = messagesContext.getAllChatMessages()
            chatMsgList.postValue(
                    msgs.map {
                        val index = msgs.indexOf(it)
                        val lastMsgTime = msgs.getOrNull(index - 1)?.timestamp ?: 0
                        val diff = it.timestamp - lastMsgTime
                        ChatMessageWrap((ceil(diff * 1.0f / 1000 / 60.toDouble())) >= 2, it) // 大于2分钟显示时间, it)
                    }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        messagesContext.unRegisterEventHandler(messagesEventHandler)
        workerThread.shutdownNow()
    }

    fun sendMsg(content: String) {
        messagesContext.sendChatMessage(content)
    }


    fun reSentMsg(messageId: Int) {
        messagesContext.reSendChatMessage(messageId)
    }

    data class ChatMessageWrap(
            val showTime: Boolean,
            val message: ChatMessage
    )

}