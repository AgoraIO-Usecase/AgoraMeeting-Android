package io.agora.meeting.ui.framework

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import io.agora.meeting.ui.R
import io.agora.meeting.ui.base.KBaseFragment
import io.agora.meeting.ui.module.main.bottom.MainBottomUC
import io.agora.meeting.ui.module.main.notify.MainNotifyUC
import io.agora.meeting.ui.module.main.render.mix.MixLayoutUC
import io.agora.meeting.ui.module.main.render.mix.MixLayoutVM
import io.agora.meeting.ui.module.main.top.MainTopUC
import io.agora.meeting.ui.module.root.RootUC

class MainFragment : KBaseFragment() {

    private var navigation: MainNavigation? = null

    private val mainTopUC by lazy { createUiController(MainTopUC::class.java) }
    private val mainBottomUC by lazy {
        createUiController(MainBottomUC::class.java).apply {
            usersClickListener = { navigation?.navigateToUsersPage(it) }
            chatClickListener = { navigation?.navigateToMessagePage(it) }
            settingClickListener = { navigation?.navigateToSettingPage(it) }
        }
    }
    private val mainNotifyUC by lazy {
        createUiController(MainNotifyUC::class.java).apply {
            settingClickListener = { navigation?.navigateToSettingPage(it) }
        }
    }
    private val mainRenderUC by lazy {
        createUiController(MixLayoutUC::class.java).apply {
            boardClickListener = { navigation?.navigateToWhiteboardPage(it) }
            layoutChangeListener = { type: MixLayoutVM.MixLayoutType, count: Int->
                if (type == MixLayoutVM.MixLayoutType.Lecturer && count > 1) {
                    mainNotifyUC.updateFloatNotifyBottom(resources.getDimensionPixelSize(R.dimen.meeting_float_notify_bottom_margin_max))
                } else {
                    mainNotifyUC.updateFloatNotifyBottom(resources.getDimensionPixelSize(R.dimen.meeting_float_notify_bottom_margin_min))
                }
            }
        }
    }
    private val rootUC by lazy {
        createUiController(RootUC::class.java).apply {
            onFinishListener = { navigation?.navigateUp() ?: requireActivity().finish() }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_fragment, container, false).apply {
            initViewBinding(this)
            postDelayed({ delayInitViewBinding(this) }, 400)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolBar()
        initArguments()
        bindViewModels()
        view.postDelayed(this::delayBindViewModels, 500)
    }

    private fun initArguments() {
        safeGetArguments {
            mainRenderUC.statsDisplayEnable = MainFragmentArgs.fromBundle(it).statsDisplayEnable
        }
    }

    private fun initToolBar() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            mainTopUC.showLeaveDialog()
        }
        setupAppBar(null, true)
    }

    private fun initViewBinding(rootView: View) {
        mainTopUC.createViewBinding(requireContext(), viewLifecycleOwner, rootView.findViewById(R.id.top_layout_container))
        mainBottomUC.createViewBinding(requireContext(), viewLifecycleOwner, rootView.findViewById(R.id.bottom_layout_container))
        mainRenderUC.createViewBinding(requireContext(), viewLifecycleOwner, rootView.findViewById(R.id.render_layout_container))
        rootUC.createViewBinding(requireContext(), viewLifecycleOwner, rootView.findViewById(R.id.render_layout_container))
    }

    private fun bindViewModels() {
        mainTopUC.bindViewModel(this)
        mainBottomUC.bindViewModel(this)
        mainRenderUC.bindViewModel(requireActivity())
        rootUC.bindViewModel(requireActivity())
    }

    private fun delayInitViewBinding(rootView: View) {
        mainNotifyUC.createViewBinding(requireContext(), viewLifecycleOwner, rootView.findViewById(R.id.notify_layout_container))
    }

    private fun delayBindViewModels() {
        mainNotifyUC.bindViewModel(this)
    }

    fun setNavigation(nav: MainNavigation?) {
        navigation = nav
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainNavigation) {
            navigation = context
        }
    }

    override fun onDetach() {
        if (navigation == requireActivity()) {
            navigation = null
        }
        super.onDetach()
    }

    interface MainNavigation {
        fun navigateUp(): Boolean
        fun navigateToUsersPage(view: View?)
        fun navigateToSettingPage(view: View?)
        fun navigateToMessagePage(view: View?)
        fun navigateToWhiteboardPage(view: View?)
    }

}