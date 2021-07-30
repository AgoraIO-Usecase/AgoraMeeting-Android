package io.agora.meeting.ui.module.main.render.mix

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.meeting.ui.base.KBaseFragment
import io.agora.meeting.ui.module.main.render.audio.AudioLayoutUC

internal class AudioLayoutFrag : KBaseFragment() {

    private val audioLayoutUC = createUiController(AudioLayoutUC::class.java)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return audioLayoutUC.createViewBinding(requireContext(), viewLifecycleOwner, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        audioLayoutUC.bindViewModel(this)
    }

}