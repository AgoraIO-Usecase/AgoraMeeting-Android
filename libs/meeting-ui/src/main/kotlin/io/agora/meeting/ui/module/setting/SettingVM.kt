package io.agora.meeting.ui.module.setting

import androidx.lifecycle.MutableLiveData
import io.agora.meeting.context.MessagesContext
import io.agora.meeting.context.PlatformContext
import io.agora.meeting.context.RoomContext
import io.agora.meeting.context.UsersContext
import io.agora.meeting.context.bean.DeviceType
import io.agora.meeting.context.bean.RoomClosedReason
import io.agora.meeting.context.bean.RoomJoinState
import io.agora.meeting.context.bean.UserInfo
import io.agora.meeting.ui.base.BaseViewModel
import io.agora.meeting.ui.base.Event

class SettingVM(
        private val roomContext: RoomContext,
        private val platformContext: PlatformContext,
        private val usersContext: UsersContext,
        private val messagesContext: MessagesContext
) : BaseViewModel() {

    val cameraAccess = MutableLiveData<Boolean>(roomContext.hasCameraAccess())
    val micAccess = MutableLiveData<Boolean>(roomContext.hasMicAccess())


    private val roomEventHandler = object: RoomContext.RoomEventHandler{

        override fun onRoomJoined(state: RoomJoinState) {

        }

        override fun onRoomClosed(reason: RoomClosedReason) {

        }

        override fun onUserPermissionChanged(device: DeviceType, access: Boolean) {
            when(device){
                DeviceType.Mic -> micAccess.postValue(access)
                DeviceType.Camera -> cameraAccess.postValue(access)
            }
        }

        override fun onFlexRoomPropertiesChanged(properties: Map<String, String>?, fromUser: UserInfo?) {

        }

    }

    init {
        roomContext.registerEventHandler(roomEventHandler)
    }

    override fun onCleared() {
        super.onCleared()
        roomContext.unRegisterEventHandler(roomEventHandler)
    }

    fun changeUserPermission(device: DeviceType, access: Boolean){
        roomContext.changeUserPermission(device, access, failure = {
            when(device){
                DeviceType.Camera -> cameraAccess.value = roomContext.hasCameraAccess()
                DeviceType.Mic -> micAccess.value = roomContext.hasMicAccess()
            }
        })
    }

    fun setUserInOutNotifyLimit(count: Int){
        messagesContext.setUserInOutNotifyLimit(count)
    }

    fun getUserInOutNotifyLimit() = messagesContext.getUserInOutNotifyLimit()

    fun uploadLog(success: (String?) -> Unit, failure: (Throwable) -> Unit){
        loadingEvent.postValue(Event(true))
        platformContext.uploadLog({
            loadingEvent.postValue(Event(false))
            success.invoke(it)
        }, {
            loadingEvent.postValue(Event(false))
            failure.invoke(it)
        })
    }

    fun getRoomInfo() = roomContext.getRoomInfo()

    fun getLocalUserInfo() = usersContext.getLocalUserInfo()
}