package io.agora.meeting.ui.module.main.bottom

import android.util.Log
import androidx.lifecycle.MutableLiveData
import io.agora.meeting.context.*
import io.agora.meeting.context.bean.*
import io.agora.meeting.ui.base.BaseViewModel
import io.agora.meeting.ui.base.Event

class MainBottomVM(
        private val roomContext: RoomContext,
        private val usersContext: UsersContext,
        private val mediaContext: MediaContext,
        private val messagesContext: MessagesContext,
        private val screenContext: ScreenContext,
        private val boardContext: BoardContext
) : BaseViewModel() {

    val cameraState = MutableLiveData<LocalDeviceInfo>(LocalDeviceInfo(mediaContext.getLocalDeviceState(DeviceType.Camera), mediaContext.getApproveLeftSecond(DeviceType.Camera)))
    val micState = MutableLiveData<LocalDeviceInfo>(LocalDeviceInfo(mediaContext.getLocalDeviceState(DeviceType.Mic), mediaContext.getApproveLeftSecond(DeviceType.Mic)))

    val unReadMsgCount = MutableLiveData(0)

    private val mediaEventHandler = object : MediaContext.MediaEventHandler {

        override fun onLocalAudioRouteChanged(route: AudioRoute) {

        }

        override fun onLocalDeviceStateChanged(device: DeviceType, state: LocalDeviceState, leftSecond: Int) {
            Log.d("MainBottomVM", "onLocalDeviceStateChanged--device=$device,state=$state,leftSecond=$leftSecond")
            when (device) {
                DeviceType.Camera -> cameraState.value = LocalDeviceInfo(state, leftSecond)
                DeviceType.Mic -> micState.value = LocalDeviceInfo(state, leftSecond)
            }
        }
    }

    private val messagesEventHandler = object : MessagesContext.MessagesEventHandler {

        override fun onChatMessagesUpdated(messages: List<ChatMessage>) {
            if (unReadMsgCount.hasActiveObservers()) {
                unReadMsgCount.postValue(unReadMsgCount.value?.plus(1))
            }
        }

        override fun onNotifyMessagesUpdated(messages: List<NotifyMessage>) {

        }

    }

    init {
        mediaContext.registerEventHandler(mediaEventHandler)
        messagesContext.registerEventHandler(messagesEventHandler)
    }


    override fun onCleared() {
        super.onCleared()
        mediaContext.unRegisterEventHandler(mediaEventHandler)
        messagesContext.unRegisterEventHandler(messagesEventHandler)
    }

    fun checkMicApproveNeed(): Boolean {
        return mediaContext.checkDevicePermission(DeviceType.Mic) == DevicePermission.ApproveNeed
    }

    fun checkCameraApproveNeed(): Boolean {
        return mediaContext.checkDevicePermission(DeviceType.Camera) == DevicePermission.ApproveNeed
    }

    fun openLocalMicDevice() {
        mediaContext.openLocalDevice(DeviceType.Mic, failure = { failureEvent.postValue(Event(it)) })
    }

    fun openLocalCameraDevice() {
        mediaContext.openLocalDevice(DeviceType.Camera, failure = { failureEvent.postValue(Event(it)) })
    }

    fun closeLocalMicDevice() {
        mediaContext.closeLocalDevice(DeviceType.Mic, failure = { failureEvent.postValue(Event(it)) })
    }

    fun closeLocalCameraDevice() {
        mediaContext.closeLocalDevice(DeviceType.Camera, failure = { failureEvent.postValue(Event(it)) })
    }

    fun getLocalUserInfo() = usersContext.getLocalUserInfo()

    fun getRoomInfo() = roomContext.getRoomInfo()

    fun getLocalUserOptions() = usersContext.getLocalUserInfo().options

    fun dealLocalUserOperation(operation: UserOperation) {
        getLocalUserInfo().let {
            usersContext.dealUserOperation(it.userId, operation)
        }
    }

    fun changeUserPermission(deviceType: DeviceType, access: Boolean) {
        roomContext.changeUserPermission(deviceType, access, failure = { failureEvent.postValue(Event(it)) })
    }

    fun cleanUnReadCount() {
        unReadMsgCount.postValue(0)
    }

    fun isMyScreenSharing() = screenContext.isScreenSharingByMyself()

    fun openScreenSharing() {
        screenContext.openScreenSharing(failure = { failureEvent.postValue(Event(it)) })
    }

    fun closeScreenSharing() {
        screenContext.closeScreenSharing(failure = { failureEvent.postValue(Event(it)) })
    }

    fun openBoardSharing() {
        boardContext.openBoardSharing(failure = { failureEvent.postValue(Event(it)) })
    }

    data class LocalDeviceInfo(
            val state: LocalDeviceState,
            val leftSecond: Int
    )
}