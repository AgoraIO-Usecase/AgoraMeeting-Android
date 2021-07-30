package io.agora.meeting.example.uicontroller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.meeting.R
import io.agora.meeting.annotation.Example
import io.agora.meeting.common.model.Examples
import io.agora.meeting.ui.base.KBaseFragment
import io.agora.meeting.ui.module.users.UsersUC

@Example(
        index = 5,
        group = Examples.GROUP_UI_CONTROLLER,
        nameStrId = R.string.fragment_users,
        tipStrId = R.string.fragment_users_tip
)
class Users: KBaseFragment() {

    private val usersUC by lazy { createUiController(UsersUC::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.layout_container, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usersUC.createViewBinding(requireContext(), viewLifecycleOwner, view.findViewById(R.id.container))
        usersUC.bindViewModel(this)
    }
}