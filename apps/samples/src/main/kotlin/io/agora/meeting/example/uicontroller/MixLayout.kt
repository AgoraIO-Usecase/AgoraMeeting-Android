package io.agora.meeting.example.uicontroller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.meeting.R
import io.agora.meeting.annotation.Example
import io.agora.meeting.common.model.Examples
import io.agora.meeting.ui.base.KBaseFragment
import io.agora.meeting.ui.module.main.render.mix.MixLayoutUC

@Example(
        index = 11,
        group = Examples.GROUP_UI_CONTROLLER,
        nameStrId = R.string.ui_controller_mix_layout,
        tipStrId = R.string.ui_controller_mix_layout_tip
)
class MixLayout : KBaseFragment() {

    private val mixLayoutUC by lazy {
        createUiController(MixLayoutUC::class.java).apply {
            statsDisplayEnable = true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.layout_container, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mixLayoutUC.createViewBinding(requireContext(), viewLifecycleOwner, view.findViewById(R.id.container))
        mixLayoutUC.bindViewModel(this)
    }

}