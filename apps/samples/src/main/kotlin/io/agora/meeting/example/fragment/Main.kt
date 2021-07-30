package io.agora.meeting.example.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.meeting.R
import io.agora.meeting.annotation.Example
import io.agora.meeting.common.BaseFragment
import io.agora.meeting.common.model.Examples
import io.agora.meeting.ui.framework.MainFragment
import io.agora.meeting.ui.framework.MainFragmentArgs
import io.agora.meeting.ui.util.ToastUtil

@Example(
        index = 1,
        group = Examples.GROUP_FRAGMENT,
        nameStrId = R.string.fragment_main,
        tipStrId = R.string.fragment_main_tip
)
class Main : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.layout_container, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        childFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment().apply {
                    arguments = MainFragmentArgs(true).toBundle()
                    setNavigation(object : MainFragment.MainNavigation{
                        override fun navigateUp(): Boolean {
                            requireActivity().finish()
                            return true
                        }

                        override fun navigateToUsersPage(view: View?) {
                            ToastUtil.showShort("navigateToUsersPage")
                        }

                        override fun navigateToSettingPage(view: View?) {
                            ToastUtil.showShort("navigateToSettingPage")
                        }

                        override fun navigateToMessagePage(view: View?) {
                            ToastUtil.showShort("navigateToMessagePage")
                        }

                        override fun navigateToWhiteboardPage(view: View?) {
                            ToastUtil.showShort("navigateToWhiteboardPage")
                        }

                    })
                })
                .commit()
    }
}