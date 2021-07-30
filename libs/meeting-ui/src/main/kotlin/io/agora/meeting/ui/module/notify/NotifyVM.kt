package io.agora.meeting.ui.module.notify

import androidx.lifecycle.MutableLiveData
import io.agora.meeting.context.MediaContext
import io.agora.meeting.context.MessagesContext
import io.agora.meeting.context.bean.ChatMessage
import io.agora.meeting.context.bean.DeviceType
import io.agora.meeting.context.bean.NotifyMessage
import io.agora.meeting.ui.base.BaseViewModel
import io.agora.meeting.ui.base.Event

class NotifyVM(
        private val mediaContext: MediaContext,
        private val messagesContext: MessagesContext
) : BaseViewModel() {


    val notifyMsgList = MutableLiveData<List<NotifyMessage>>(messagesContext.getAllNotifyMessage())

    private val messagesEventHandler = object : MessagesContext.MessagesEventHandler {

        override fun onChatMessagesUpdated(messages: List<ChatMessage>) {

        }

        override fun onNotifyMessagesUpdated(messages: List<NotifyMessage>) {
            notifyMsgList.postValue(messages)
        }

    }

    init {
        messagesContext.registerEventHandler(messagesEventHandler)
    }

    override fun onCleared() {
        super.onCleared()
        messagesContext.unRegisterEventHandler(messagesEventHandler)
    }

    fun handleMsgEvent(messageId: Int) {
        messagesContext.dealNotifyMessageEvent(messageId, failure = { failureEvent.postValue(Event(it))})
    }

    fun getInOutLimit() = messagesContext.getUserInOutNotifyLimit()

    fun getCamApproveEffectiveSecond() = mediaContext.getApproveEffectiveSecond(DeviceType.Camera)

    fun getMicApproveEffectiveSecond() = mediaContext.getApproveEffectiveSecond(DeviceType.Mic)
}