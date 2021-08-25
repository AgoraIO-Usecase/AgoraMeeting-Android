package io.agora.meeting.ui.module.root

import android.util.Log
import androidx.lifecycle.MutableLiveData
import io.agora.meeting.context.RoomContext
import io.agora.meeting.context.UsersContext
import io.agora.meeting.context.bean.*
import io.agora.meeting.ui.BuildConfig
import io.agora.meeting.ui.base.BaseViewModel
import io.agora.meeting.ui.base.Event
import io.agora.meeting.ui.util.ToastUtil

class RootVM(
        private val roomContext: RoomContext,
        private val usersContext: UsersContext
) : BaseViewModel() {

    val roomCloseEvent = MutableLiveData<Event<RoomClosedReason>>()
    val kickOutEvent = MutableLiveData<Event<RoomInfo>>()
    val roomJoinFailEvent = MutableLiveData<Event<Boolean>>(Event(roomContext.getRoomJoinState() == RoomJoinState.JOIN_FAILED))


    private val roomEventHandler = object : RoomContext.RoomEventHandler {

        override fun onRoomJoined(state: RoomJoinState) {
            roomJoinFailEvent.postValue(Event(state == RoomJoinState.JOIN_FAILED))


            showUserPropertiesInfo()

        }

        override fun onRoomClosed(reason: RoomClosedReason) {
            roomCloseEvent.postValue(Event(reason))
        }

        override fun onUserPermissionChanged(device: DeviceType, access: Boolean) {

        }

    }

    private fun showUserPropertiesInfo() {
        if(!BuildConfig.DEBUG){
            return
        }
        val userInfoList = usersContext.getUserInfoList()
        if(userInfoList.isEmpty()) return

        var propertiesInfo = "------UserPropertiesInfo------\n"
        propertiesInfo += "userInfoList size=${userInfoList.size}\n"
        userInfoList.forEach {
            val userProperties = usersContext.getUserProperties(it.userId)
            propertiesInfo += "userId=${it.userId}, userProperties=$userProperties\n"
        }
        ToastUtil.showLong(propertiesInfo)
    }

    private val usersEventHandler = object : UsersContext.UsersEventHandler {

        override fun onLocalConnectStateChanged(state: ConnectState) {
            Log.d("RoomVM", "onLocalConnectStateChanged state=$state")
        }

        override fun onUserListChanged(userInfoList: List<UserDetailInfo>) {
            showUserPropertiesInfo()
        }

        override fun onKickedOut() {
            kickOutEvent.postValue(Event(roomContext.getRoomInfo()))
        }

        override fun onUserPropertiesUpdate(userId: String, full: Map<String, Any>?) {
            Log.d("RoomVM", "onUserPropertiesUpdate userId=$userId, full=$full")
            if(BuildConfig.DEBUG){
                ToastUtil.showLong("onUserPropertiesUpdate userId=$userId, full=$full")
            }
        }

    }

    init {
        roomContext.registerEventHandler(roomEventHandler)
        usersContext.registerEventHandler(usersEventHandler)
    }

    override fun onCleared() {
        super.onCleared()
        roomContext.unRegisterEventHandler(roomEventHandler)
        usersContext.unRegisterEventHandler(usersEventHandler)
    }

}