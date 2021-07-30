package io.agora.meeting.example.uicontroller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.meeting.R
import io.agora.meeting.annotation.Example
import io.agora.meeting.common.model.Examples
import io.agora.meeting.ui.base.KBaseFragment
import io.agora.meeting.ui.module.board.BoardUC
import io.agora.meeting.ui.util.ToastUtil

@Example(
        index = 8,
        group = Examples.GROUP_UI_CONTROLLER,
        nameStrId = R.string.fragment_board,
        tipStrId = R.string.fragment_board_tip
)
class Board : KBaseFragment() {

    private val boardUC by lazy {
        createUiController(BoardUC::class.java).apply {
            onCloseListener = {
                if(it){
                    ToastUtil.showShort("Whiteboard sharing is not turned on")
                }

                requireActivity().finish()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.layout_container, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        boardUC.createViewBinding(requireContext(), viewLifecycleOwner, view.findViewById(R.id.container))
        boardUC.bindViewModel(this)
    }
}