package io.agora.meeting.ui.module.board

import androidx.lifecycle.MutableLiveData
import io.agora.meeting.context.BoardContext
import io.agora.meeting.context.bean.BoardAppliance
import io.agora.meeting.context.bean.BoardPermission
import io.agora.meeting.ui.base.BaseViewModel
import io.agora.meeting.ui.base.Event

class BoardVM(
        private val boardContext: BoardContext
) : BaseViewModel() {

    val boardPermission = MutableLiveData<BoardPermission>(boardContext.getBoardPermission())
    val boardState = MutableLiveData<Boolean>(boardContext.isBoardSharing())

    private val boardEventHandler = object : BoardContext.BoardEventHandler {

        override fun onBoardStateChanged(isOpen: Boolean) {
            // close the page
            boardState.value = isOpen
        }

        override fun onBoardPermissionChanged(permission: BoardPermission) {
            // change the tool visible
            boardPermission.value = permission
        }
    }

    init {
        boardContext.registerEventHandler(boardEventHandler)
    }

    override fun onCleared() {
        super.onCleared()
        boardContext.unRegisterEventHandler(boardEventHandler)
    }

    fun getWhiteBoardView(detach: Boolean) = boardContext.getWhiteBoardView(detach)

    fun getBoardStrokeColor() = boardContext.getStrokeColor()

    fun changeBoardStrokeColor(color: IntArray) {
        boardContext.changeStrokeColor(color)
    }

    fun isBoardSharing() = boardContext.isBoardSharing()

    fun cleanBoardContent() {
        boardContext.cleanBoardContent()
    }

    fun setBoardAppliance(appliance: String) {
        boardContext.setAppliance(BoardAppliance.valueOf(appliance))
    }

    fun setBoardWritable(writable: Boolean) {
        boardContext.setWritable(writable)
    }

    fun applyBoard() {
        boardContext.applyBoardInteract(failure = { failureEvent.postValue(Event(it)) })
    }

    fun cancelBoard() {
        boardContext.cancelBoardInteract(failure = { failureEvent.postValue(Event(it)) })
    }

    fun closeBoard() {
        boardContext.closeBoardSharing(failure = { failureEvent.postValue(Event(it)) })
    }

}