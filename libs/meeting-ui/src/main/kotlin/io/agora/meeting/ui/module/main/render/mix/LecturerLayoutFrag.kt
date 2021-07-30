package io.agora.meeting.ui.module.main.render.mix

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import io.agora.meeting.ui.base.KBaseFragment
import io.agora.meeting.ui.module.main.render.lecturer.LecturerLayoutUC

internal class LecturerLayoutFrag(private val viewModel: MixLayoutVM) : KBaseFragment(){

    var navigation: LecturerLayoutNavigation?= null

    private val lecturerLayoutUC by lazy {
        createUiController(LecturerLayoutUC::class.java).apply {
            statsDisplayEnable = viewModel.statsDisplayEnable.value ?: false
            mainRenderId = arguments?.getInt(ARGUMENTS_MAIN_RENDER_ID) ?: -1
            boardClickListener = {
                navigation?.navigateToWhiteboardPage(it)
            }
            tiledLayoutClickListener = {
                navigation?.navigateToTiledLayout(it)
            }
        }
    }

    companion object{
        private const val ARGUMENTS_MAIN_RENDER_ID = "main_render_id"

        fun createInstance(viewModel: MixLayoutVM, renderId: Int = -1) =
                LecturerLayoutFrag(viewModel).apply {
                    arguments = Bundle().apply {
                        putInt(ARGUMENTS_MAIN_RENDER_ID, renderId)
                    }
                }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return lecturerLayoutUC.createViewBinding(requireContext(), viewLifecycleOwner, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.statsDisplayEnable.observe(viewLifecycleOwner) {
            lecturerLayoutUC.statsDisplayEnable = it
        }

        lecturerLayoutUC.bindViewModel(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is LecturerLayoutNavigation){
            navigation = context
        }
    }

    override fun onDetach() {
        if(navigation === requireContext()){
            navigation = null
        }
        super.onDetach()
    }

    override fun onDestroy() {
        super.onDestroy()
        navigation = null
    }

    interface LecturerLayoutNavigation {
        fun navigateToTiledLayout(view: View?)
        fun navigateToWhiteboardPage(view: View?)
    }
}