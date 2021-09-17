package io.agora.meeting.ui.module.main.notify

import androidx.lifecycle.MutableLiveData
import io.agora.meeting.context.MediaContext
import io.agora.meeting.context.MessagesContext
import io.agora.meeting.context.bean.ChatMessage
import io.agora.meeting.context.bean.DeviceType
import io.agora.meeting.context.bean.NotifyMessage
import io.agora.meeting.context.bean.UserInfo
import io.agora.meeting.ui.base.BaseViewModel
import io.agora.meeting.ui.base.Event
import java.util.*

class MainNotifyVM(
        private val mediaContext: MediaContext,
        private val messagesContext: MessagesContext
) : BaseViewModel() {


    val notifyMsgList = MutableLiveData<List<NotifyMessage>>()
    var maxShowCount = 3

    private var firstMsgId = 0
    private val timer = Timer()
    private var timerTask: TimerTask? = null

    private val messagesEventHandler = object : MessagesContext.MessagesEventHandler {

        override fun onChatMessagesUpdated(messages: List<ChatMessage>) {
            // do nothing
        }

        override fun onNotifyMessagesUpdated(messages: List<NotifyMessage>) {
            notifyMsgList.postValue(takeNotifyMsg(messages))
            startTiming()
        }

        override fun onPrivateChatMessageReceived(content: String, fromUser: UserInfo) {

        }

    }

    fun initialize() {
        messagesContext.getAllNotifyMessage().apply {
            if (isNotEmpty()) {
                notifyMsgList.value = takeNotifyMsg(this)
                startTiming()
            }
        }
    }

    private fun takeNotifyMsg(messages: List<NotifyMessage>) = messages.takeLastWhile { it.messageId >= firstMsgId }
            .takeLast(maxShowCount)
            .apply {
                firstMsgId = firstOrNull()?.messageId ?: firstMsgId
            }

    private fun startTiming() {
        timerTask?.cancel()
        timer.purge()
        timerTask = object : TimerTask() {
            override fun run() {
                removeFirstNotify()
            }
        }.apply {
            timer.schedule(this, 10000, 10000)
        }
    }

    private fun stopTiming() {
        timerTask?.cancel()
        timerTask = null
        timer.purge()
    }


    init {
        messagesContext.registerEventHandler(messagesEventHandler)
    }

    override fun onCleared() {
        super.onCleared()
        messagesContext.unRegisterEventHandler(messagesEventHandler)
        timer.cancel()
    }

    fun handleMsgEvent(messageId: Int) {
        messagesContext.dealNotifyMessageEvent(messageId, failure = { failureEvent.postValue(Event(it))})
    }

    fun getInOutLimit() = messagesContext.getUserInOutNotifyLimit()

    fun getCamApproveEffectiveSecond() = mediaContext.getApproveEffectiveSecond(DeviceType.Camera)

    fun getMicApproveEffectiveSecond() = mediaContext.getApproveEffectiveSecond(DeviceType.Mic)

    private fun removeFirstNotify() {
        notifyMsgList.value?.drop(1)?.let {
            firstMsgId = it.firstOrNull()?.messageId ?: firstMsgId + 1
            notifyMsgList.postValue(it)
            if (it.isEmpty()) {
                stopTiming()
            }
        }
    }
}