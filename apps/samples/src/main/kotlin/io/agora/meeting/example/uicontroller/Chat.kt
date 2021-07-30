package io.agora.meeting.example.uicontroller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.meeting.R
import io.agora.meeting.annotation.Example
import io.agora.meeting.common.model.Examples
import io.agora.meeting.ui.base.KBaseFragment
import io.agora.meeting.ui.module.chat.ChatUC

@Example(
        index = 6,
        group = Examples.GROUP_UI_CONTROLLER,
        nameStrId = R.string.fragment_chat,
        tipStrId = R.string.fragment_chat_tip
)
class Chat: KBaseFragment() {

    private val chatUC by lazy { createUiController(ChatUC::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.layout_container, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatUC.createViewBinding(requireContext(), viewLifecycleOwner, view.findViewById(R.id.container))
        chatUC.bindViewModel(this)
    }
}