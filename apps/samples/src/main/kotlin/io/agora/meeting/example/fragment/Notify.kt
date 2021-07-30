package io.agora.meeting.example.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.meeting.R
import io.agora.meeting.annotation.Example
import io.agora.meeting.common.BaseFragment
import io.agora.meeting.common.model.Examples
import io.agora.meeting.ui.framework.NotifyFragment
import io.agora.meeting.ui.util.ToastUtil

@Example(
        index = 5,
        group = Examples.GROUP_FRAGMENT,
        nameStrId = R.string.fragment_notify,
        tipStrId = R.string.fragment_notify_tip
)
class Notify: BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.layout_container, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        childFragmentManager.beginTransaction()
                .replace(R.id.container, NotifyFragment().apply {
                    setNavigation(object : NotifyFragment.NotifyNavigation{
                        override fun navigateToSettingPage(view: View?) {
                            ToastUtil.showShort("navigateToSettingPage")
                        }
                    })
                })
                .commit()
    }
}