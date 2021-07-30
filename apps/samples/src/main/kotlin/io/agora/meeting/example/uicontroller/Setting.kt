package io.agora.meeting.example.uicontroller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.meeting.R
import io.agora.meeting.annotation.Example
import io.agora.meeting.common.model.Examples
import io.agora.meeting.ui.base.KBaseFragment
import io.agora.meeting.ui.module.setting.SettingUC

@Example(
        index = 9,
        group = Examples.GROUP_UI_CONTROLLER,
        nameStrId = R.string.fragment_setting,
        tipStrId = R.string.fragment_setting_tip
)
class Setting: KBaseFragment() {

    private val settingUC by lazy { createUiController(SettingUC::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.layout_container, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingUC.createViewBinding(requireContext(), viewLifecycleOwner, view.findViewById(R.id.container))
        settingUC.bindViewModel(this)
    }
}