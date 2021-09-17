package io.agora.meeting.ui.module.notify

import androidx.lifecycle.MutableLiveData
import io.agora.meeting.context.MediaContext
import io.agora.meeting.context.MessagesContext
import io.agora.meeting.context.bean.ChatMessage
import io.agora.meeting.context.bean.DeviceType
import io.agora.meeting.context.bean.NotifyMessage
import io.agora.meeting.context.bean.UserInfo
import io.agora.meeting.ui.base.BaseViewModel
import io.agora.meeting.ui.base.Event
import java.util.concurrent.Executors
import kotlin.math.ceil

class NotifyVM(
        private val mediaContext: MediaContext,
        private val messagesContext: MessagesContext
) : BaseViewModel() {

    private val workerThread = Executors.newSingleThreadScheduledExecutor()

    val notifyMsgList = MutableLiveData<List<NotifyMessageWrap>>()

    private val messagesEventHandler = object : MessagesContext.MessagesEventHandler {

        override fun onChatMessagesUpdated(messages: List<ChatMessage>) {

        }

        override fun onNotifyMessagesUpdated(messages: List<NotifyMessage>) {
            updateList()
        }

        override fun onPrivateChatMessageReceived(content: String, fromUser: UserInfo) {

        }

    }

    init {
        messagesContext.registerEventHandler(messagesEventHandler)
        updateList()
    }

    private fun updateList(){
        workerThread.submit{
            val msgs = messagesContext.getAllNotifyMessage()
            notifyMsgList.postValue(
                    msgs.map {
                        val index = msgs.indexOf(it)
                        val lastMsgTime = msgs.getOrNull(index - 1)?.timestamp ?: 0
                        val diff = it.timestamp - lastMsgTime
                        NotifyMessageWrap((ceil(diff * 1.0f / 1000 / 60.toDouble())) >= 2, it) // 大于2分钟显示时间, it)
                    }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        messagesContext.unRegisterEventHandler(messagesEventHandler)
        workerThread.shutdownNow()
    }

    fun handleMsgEvent(messageId: Int) {
        messagesContext.dealNotifyMessageEvent(messageId, failure = { failureEvent.postValue(Event(it))})
    }

    fun getInOutLimit() = messagesContext.getUserInOutNotifyLimit()

    fun getCamApproveEffectiveSecond() = mediaContext.getApproveEffectiveSecond(DeviceType.Camera)

    fun getMicApproveEffectiveSecond() = mediaContext.getApproveEffectiveSecond(DeviceType.Mic)

    data class NotifyMessageWrap(
            val showTime: Boolean,
            val message: NotifyMessage
    )
}