package io.agora.meeting.ui.framework

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.meeting.ui.base.KBaseFragment
import io.agora.meeting.ui.module.notify.NotifyUC

class NotifyFragment : KBaseFragment() {

    private var navigation: NotifyNavigation? = null

    private val notifyUC by lazy {
        createUiController(NotifyUC::class.java).apply {
            settingClickListener = { navigation?.navigateToSettingPage(it) }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return notifyUC.createViewBinding(requireContext(), viewLifecycleOwner, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        notifyUC.bindViewModel(this)
    }

    fun setNavigation(nav: NotifyNavigation?) {
        navigation = nav
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is NotifyNavigation) {
            navigation = context
        }
    }

    override fun onDetach() {
        if (navigation == requireActivity()) {
            navigation = null
        }
        super.onDetach()
    }

    interface NotifyNavigation {
        fun navigateToSettingPage(view: View?)
    }
}