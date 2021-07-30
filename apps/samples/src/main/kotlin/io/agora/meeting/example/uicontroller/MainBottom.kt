package io.agora.meeting.example.uicontroller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.meeting.R
import io.agora.meeting.annotation.Example
import io.agora.meeting.common.model.Examples
import io.agora.meeting.ui.base.KBaseFragment
import io.agora.meeting.ui.module.main.bottom.MainBottomUC
import io.agora.meeting.ui.util.ToastUtil

@Example(
        index = 2,
        group = Examples.GROUP_UI_CONTROLLER,
        nameStrId = R.string.ui_controller_main_bottom,
        tipStrId = R.string.ui_controller_main_bottom_tip
)
class MainBottom : KBaseFragment() {

    private val mainBottomUC by lazy {
        createUiController(MainBottomUC::class.java).apply {
            settingClickListener = { ToastUtil.showShort("settingClickListener") }
            usersClickListener = { ToastUtil.showShort("usersClickListener") }
            chatClickListener = { ToastUtil.showShort("chatClickListener") }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.layout_container, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainBottomUC.createViewBinding(requireContext(), viewLifecycleOwner, view.findViewById(R.id.container))

        mainBottomUC.bindViewModel(this)
    }

}