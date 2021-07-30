package io.agora.meeting.ui.module.chat

import androidx.lifecycle.MutableLiveData
import io.agora.meeting.context.MessagesContext
import io.agora.meeting.context.bean.ChatMessage
import io.agora.meeting.context.bean.NotifyMessage
import io.agora.meeting.ui.base.BaseViewModel

class ChatVM(
        private val messagesContext: MessagesContext
) : BaseViewModel() {

    val chatMsgList = MutableLiveData<List<ChatMessage>>(messagesContext.getAllChatMessages())

    private val messagesEventHandler = object : MessagesContext.MessagesEventHandler {

        override fun onChatMessagesUpdated(messages: List<ChatMessage>) {
            chatMsgList.postValue(messages)
        }

        override fun onNotifyMessagesUpdated(messages: List<NotifyMessage>) {

        }

    }

    init {
        messagesContext.registerEventHandler(messagesEventHandler)
    }

    override fun onCleared() {
        super.onCleared()
        messagesContext.unRegisterEventHandler(messagesEventHandler)
    }

    fun sendMsg(content: String) {
        messagesContext.sendChatMessage(content)
    }


    fun reSentMsg(messageId: Int) {
        messagesContext.reSendChatMessage(messageId)
    }

}