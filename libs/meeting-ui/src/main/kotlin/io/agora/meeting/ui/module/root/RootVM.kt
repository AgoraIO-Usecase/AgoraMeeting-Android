package io.agora.meeting.ui.module.root

import androidx.lifecycle.MutableLiveData
import io.agora.meeting.context.RoomContext
import io.agora.meeting.context.UsersContext
import io.agora.meeting.context.bean.*
import io.agora.meeting.ui.base.BaseViewModel
import io.agora.meeting.ui.base.Event

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
        }

        override fun onRoomClosed(reason: RoomClosedReason) {
            roomCloseEvent.postValue(Event(reason))
        }

        override fun onUserPermissionChanged(device: DeviceType, access: Boolean) {

        }

    }

    private val usersEventHandler = object : UsersContext.UsersEventHandler {

        override fun onUserListChanged(userInfoList: List<UserDetailInfo>) {

        }

        override fun onKickedOut() {
            kickOutEvent.postValue(Event(roomContext.getRoomInfo()))
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