package io.agora.meeting.ui.framework

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.meeting.ui.R
import io.agora.meeting.ui.base.KBaseFragment
import io.agora.meeting.ui.module.setting.SettingUC

class SettingFragment : KBaseFragment() {

    private val settingUC by lazy {
        createUiController(SettingUC::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.setting_fragment, container, false).apply {
            settingUC.createViewBinding(requireContext(), viewLifecycleOwner, findViewById(R.id.container), true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAppBar(requireView().findViewById(R.id.toolbar), false, getString(R.string.setting_title))
        settingUC.bindViewModel(this)
    }

}