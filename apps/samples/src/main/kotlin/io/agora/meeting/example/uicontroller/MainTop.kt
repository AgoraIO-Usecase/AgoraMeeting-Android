package io.agora.meeting.example.uicontroller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.meeting.R
import io.agora.meeting.annotation.Example
import io.agora.meeting.common.model.Examples
import io.agora.meeting.ui.base.KBaseFragment
import io.agora.meeting.ui.module.main.top.MainTopUC

@Example(
        index = 1,
        group = Examples.GROUP_UI_CONTROLLER,
        nameStrId = R.string.ui_controller_main_top,
        tipStrId = R.string.ui_controller_main_top_tip
)
class MainTop : KBaseFragment() {

    private val mainTopUC by lazy {
        createUiController(MainTopUC::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.layout_container, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainTopUC.createViewBinding(requireContext(), viewLifecycleOwner, view.findViewById(R.id.container))

        mainTopUC.bindViewModel(this)
    }

}