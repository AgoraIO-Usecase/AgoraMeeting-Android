package io.agora.meeting.example.uicontroller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.meeting.R
import io.agora.meeting.annotation.Example
import io.agora.meeting.common.model.Examples
import io.agora.meeting.ui.base.KBaseFragment
import io.agora.meeting.ui.module.main.notify.MainNotifyUC
import io.agora.meeting.ui.util.ToastUtil

@Example(
        index = 4,
        group = Examples.GROUP_UI_CONTROLLER,
        nameStrId = R.string.ui_controller_main_notify,
        tipStrId = R.string.ui_controller_main_notify_tip
)
class MainNotify : KBaseFragment() {

    private val mainNotifyUC by lazy {
        createUiController(MainNotifyUC::class.java).apply {
            settingClickListener = { ToastUtil.showShort("settingClickListener")}
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.layout_container, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainNotifyUC.createViewBinding(requireContext(), viewLifecycleOwner, view.findViewById(R.id.container))

        mainNotifyUC.bindViewModel(this)
    }
    
}