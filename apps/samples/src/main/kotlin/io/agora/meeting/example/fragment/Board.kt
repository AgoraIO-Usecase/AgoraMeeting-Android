package io.agora.meeting.example.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.meeting.R
import io.agora.meeting.annotation.Example
import io.agora.meeting.common.BaseFragment
import io.agora.meeting.common.model.Examples
import io.agora.meeting.ui.framework.BoardFragment

@Example(
        index = 6,
        group = Examples.GROUP_FRAGMENT,
        nameStrId = R.string.fragment_board,
        tipStrId = R.string.fragment_board_tip
)
class Board: BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.layout_container, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        childFragmentManager.beginTransaction()
                .replace(R.id.container, BoardFragment().apply {
                    setNavigation(object : BoardFragment.BoardNavigation{
                        override fun navigateUp(): Boolean {
                            requireActivity().finish()
                            return true
                        }
                    })
                })
                .commit()
    }
}