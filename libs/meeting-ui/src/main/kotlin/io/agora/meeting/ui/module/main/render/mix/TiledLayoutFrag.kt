package io.agora.meeting.ui.module.main.render.mix

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import io.agora.meeting.context.bean.RenderInfo
import io.agora.meeting.ui.base.KBaseFragment
import io.agora.meeting.ui.module.main.render.tiled.TiledVPLayoutUC

internal class TiledLayoutFrag(private val viewModel: MixLayoutVM) : KBaseFragment() {

    var navigation: TiledLayoutNavigation?= null
    private val tiledLayoutUC = createUiController(TiledVPLayoutUC::class.java).apply {
        statsDisplayEnable = viewModel.statsDisplayEnable.value ?: false
        onItemClickListener = { view: View?, renderInfo: RenderInfo ->
            navigation?.navigateToLecturerLayout(view, renderInfo)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return tiledLayoutUC.createViewBinding(requireContext(), viewLifecycleOwner, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.statsDisplayEnable.observe(viewLifecycleOwner){
            tiledLayoutUC.statsDisplayEnable = it
        }

        tiledLayoutUC.bindViewModel(requireActivity())
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is TiledLayoutNavigation){
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

    interface TiledLayoutNavigation {
        fun navigateToLecturerLayout(view: View?, renderInfo: RenderInfo)
    }
}