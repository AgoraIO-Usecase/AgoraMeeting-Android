package io.agora.meeting.ui.module.board

import androidx.lifecycle.observe
import io.agora.meeting.context.bean.BoardPermission
import io.agora.meeting.ui.base.BaseUiController
import io.agora.meeting.ui.databinding.BoardLayoutBinding
import io.agora.meeting.ui.widget.WhiteBoardWrapView
import io.agora.whiteboard.netless.widget.WhiteBoardView

class BoardUC : BaseUiController<BoardLayoutBinding, BoardVM>(BoardLayoutBinding::class.java, BoardVM::class.java) {

    var onCloseListener: ((Boolean) -> Unit)? = null

    override fun onViewModelBind() {
        super.onViewModelBind()
        if (!requireViewModel().isBoardSharing()) {
            onCloseListener?.invoke(true)
            return
        }

        requireViewModel().boardState.observe(requireLifecycleOwner()) {
            if (!it) {
                onCloseListener?.invoke(false)
            }
        }

        requireBinding().apply {
            requireViewModel().changeBoardStrokeColor(intArrayOf(252, 58, 63))
            boardView.startup(requireViewModel().getWhiteBoardView(true) as WhiteBoardView?)
            boardView.setOnSelectListener(object : WhiteBoardWrapView.OnSelectListener {

                override fun onStrokeColorSelectedBefore() =
                        requireViewModel().getBoardStrokeColor()

                override fun onApplianceSelected(appliance: String?) {
                    requireViewModel().setBoardAppliance(appliance!!)
                }

                override fun onStrokeColorSelectedAfter(color: IntArray?) {
                    requireViewModel().changeBoardStrokeColor(color!!)
                }

                override fun onCleanSelected() {
                    requireViewModel().cleanBoardContent()
                }
            })
            requireViewModel().setBoardWritable(true)
            requireViewModel().boardPermission.observe(requireLifecycleOwner()){
                boardView.setToolsVisible(it != BoardPermission.Audience)
            }
        }
    }

    fun closeBoard() {
        requireViewModel().closeBoard()
    }

    fun applyBoard() {
        requireViewModel().applyBoard()
    }

    fun quitBoard() {
        requireViewModel().cancelBoard()
    }


}