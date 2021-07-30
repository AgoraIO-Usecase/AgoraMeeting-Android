package io.agora.meeting.ui.framework

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy
import io.agora.meeting.ui.R
import io.agora.meeting.ui.base.KBaseFragment
import io.agora.meeting.ui.util.KeyboardUtil

class MessageFragment : KBaseFragment() {

    private var navigation: MessageNavigation? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.message_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        setupAppBar(toolbar, false, getString(R.string.main_message))


        toolbar.setNavigationOnClickListener {
            KeyboardUtil.hideInput(requireActivity())
            navigation?.navigateUp() ?: requireActivity().finish()
        }

        val tabLayout = view.findViewById<TabLayout>(R.id.tablayout)
        val viewPager2 = view.findViewById<ViewPager2>(R.id.viewpager2)

        viewPager2.adapter = object : FragmentStateAdapter(this) {

            override fun getItemCount() = 2

            override fun createFragment(position: Int) = when (position) {
                0 -> ChatFragment()
                else -> NotifyFragment()
            }
        }
        TabLayoutMediator(tabLayout, viewPager2, TabConfigurationStrategy { tab, position ->
            tab.setText(if (position == 0) R.string.main_chat else R.string.main_notification)
        }).attach()
    }

    fun setNavigation(nav: MessageNavigation) {
        navigation = nav
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is MessageNavigation){
            navigation = context
        }
    }

    override fun onDetach() {
        if(navigation == requireActivity()){
            navigation = null
        }
        super.onDetach()
    }

    interface MessageNavigation {
        fun navigateUp(): Boolean
    }
}