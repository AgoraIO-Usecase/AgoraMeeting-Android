package io.agora.meeting.example.uicontroller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.meeting.R
import io.agora.meeting.annotation.Example
import io.agora.meeting.common.model.Examples
import io.agora.meeting.ui.base.KBaseFragment
import io.agora.meeting.ui.module.main.render.lecturer.LecturerLayoutUC

@Example(
        index = 12,
        group = Examples.GROUP_UI_CONTROLLER,
        nameStrId = R.string.ui_controller_lecturer_layout,
        tipStrId = R.string.ui_controller_lecturer_layout_tip
)
class LecturerLayout : KBaseFragment() {

    private val lecturerLayoutUC by lazy {
        createUiController(LecturerLayoutUC::class.java).apply {
            statsDisplayEnable = true
            mainRenderId = 1
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.layout_container, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lecturerLayoutUC.createViewBinding(requireContext(), viewLifecycleOwner, view.findViewById(R.id.container))
        lecturerLayoutUC.bindViewModel(this)
    }
}