package io.agora.meeting.ui.framework

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.meeting.ui.base.KBaseFragment
import io.agora.meeting.ui.module.chat.ChatUC

class ChatFragment : KBaseFragment() {

    private val chatUC = createUiController(ChatUC::class.java)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return chatUC.createViewBinding(requireContext(), viewLifecycleOwner, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatUC.bindViewModel(requireActivity())
    }
}