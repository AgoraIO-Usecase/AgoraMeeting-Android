package io.agora.meeting.ui.module.main.top

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import io.agora.meeting.context.*
import io.agora.meeting.context.bean.*
import io.agora.meeting.ui.BuildConfig
import io.agora.meeting.ui.base.BaseViewModel
import io.agora.meeting.ui.base.Event
import io.agora.meeting.ui.util.TimeUtil

class MainTopVM(
        private val roomContext: RoomContext,
        private val usersContext: UsersContext,
        private val mediaContext: MediaContext,
        private val screenContext: ScreenContext,
        private val boardContext: BoardContext
) : BaseViewModel() {

    val roomName = MutableLiveData<String>()
    val roomCountDownText = MutableLiveData<String>()
    val audioRouteIcon = MutableLiveData<AudioRoute>(mediaContext.getLocalAudioRoute())
    val leaveClickEvent = MutableLiveData<Event<Boolean>>()
    val copyRoomEvent = MutableLiveData<Event<CopyInfo>>()

    private var countDownTimer: CountDownTimer? = null

    private val mediaEventHandler = object : MediaContext.MediaEventHandler {

        override fun onLocalAudioRouteChanged(route: AudioRoute) {
            audioRouteIcon.postValue(route)
        }

        override fun onLocalDeviceStateChanged(device: DeviceType, state: LocalDeviceState, leftSecond: Int) {

        }
    }

    init {
        mediaContext.registerEventHandler(mediaEventHandler)
        roomName.value = roomContext.getRoomInfo().roomName
        startCountDownTimer()
    }

    private fun startCountDownTimer() {
        val durationS = roomContext.getRoomInfo().durationS
        val startTimestamp = roomContext.getRoomInfo().startMileTime
        val leftMiles = durationS * 1000 - (TimeUtil.getSyncCurrentTimeMillis() - startTimestamp)
        countDownTimer = object : CountDownTimer(leftMiles, 1000) {
            override fun onFinish() {
                roomCountDownText.postValue(TimeUtil.stringForTimeHMS(durationS * 1000, "%02d:%02d:%02d"))
            }

            override fun onTick(millisUntilFinished: Long) {
                roomCountDownText.postValue(TimeUtil.stringForTimeHMS(durationS * 1000 - millisUntilFinished, "%02d:%02d:%02d"))
            }
        }.start()
    }

    private fun stopCountDownTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    fun switchCamera() {
        if (BuildConfig.DEBUG) {
            usersContext.updateLocalUserProperties(mapOf(Pair("111", "222"), Pair("aaa", "bbb")))
        }
        mediaContext.switchLocalCamera()
    }

    fun switchMic() {
        mediaContext.switchLocalMic()
    }

    fun clickLeave() {
        leaveClickEvent.postValue(Event(roomContext.canCloseRoomOrNot()))
    }

    fun copyRoomInfo() {
        usersContext.getLocalUserInfo().let {
            copyRoomEvent.postValue(Event(CopyInfo(roomContext.getRoomInfo(), it)))
        }
    }

    fun leaveRoom() {
        roomContext.leaveRoom()
        stopCountDownTimer()
    }

    fun closeRoom() {
        roomContext.closeRoom(success = { stopCountDownTimer() }, failure = { failureEvent.postValue(Event(it)) })
    }

    fun canCloseRoom() = roomContext.canCloseRoomOrNot()

    fun isScreenSharingByMe() = screenContext.isScreenSharingByMyself()

    fun stopScreenSharing() {
        screenContext.closeScreenSharing()
    }

    fun isBoardSharingByMe() = boardContext.isBoardSharingByMyself()

    fun stopBoardSharing() {
        boardContext.closeBoardSharing()
    }

    override fun onCleared() {
        super.onCleared()
        stopCountDownTimer()
        mediaContext.unRegisterEventHandler(mediaEventHandler)
    }

    data class CopyInfo(val roomInfo: RoomInfo, val userInfo: UserDetailInfo)
}