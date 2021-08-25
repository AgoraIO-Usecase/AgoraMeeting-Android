package io.agora.meeting.ui.module.main.render.audio

import androidx.core.view.isVisible
import androidx.lifecycle.observe
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.agora.meeting.ui.base.BaseUiController
import io.agora.meeting.ui.databinding.MainRenderAudioContainerBinding
import kotlin.math.ceil

class AudioLayoutUC : BaseUiController<MainRenderAudioContainerBinding, AudioLayoutVM>(
        MainRenderAudioContainerBinding::class.java,
        AudioLayoutVM::class.java
) {
    private var adapter: PageAdapter? = null

    private var lastLayoutUpdateSize = 0

    override fun onViewCreated() {
        super.onViewCreated()
        lastLayoutUpdateSize = 0
        adapter = PageAdapter()
        requireBinding().viewpager2.offscreenPageLimit = 2
        requireBinding().viewpager2.adapter = adapter

        TabLayoutMediator(requireBinding().pageTabLayout, requireBinding().viewpager2, object : TabLayoutMediator.TabConfigurationStrategy {
            override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
                binding ?: return
                val total = adapter?.itemCount ?: 0
                requireBinding().pageIndicatorText.isVisible = total > 4
                requireBinding().pageTabLayout.isVisible = total <= 4
            }
        }).attach()

        requireBinding().pageTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding ?: return
                val tabCount = requireBinding().pageTabLayout.tabCount
                val tabIndex = tab?.position ?: 0
                requireBinding().pageIndicatorText.text = "${tabIndex + 1}/$tabCount"
            }

        })
    }

    override fun onViewModelBind() {
        super.onViewModelBind()
        requireViewModel().renderInfoList.observe(requireLifecycleOwner()) {
            it.forEach {
                requireViewModel().subscriptAudio(it.streamId)
            }
            adapter?.notifyDataSetChanged()
        }
    }


    inner class PageAdapter : FragmentStateAdapter(requireFragmentManager(), requireLifecycleOwner().lifecycle) {
        override fun getItemCount() : Int{
            val size = viewModel?.renderInfoList?.value?.size ?: 0
            return ceil(size * 1.0f / AudioPageFrag.PAGE_MAX_COLUMN / AudioPageFrag.PAGE_MAX_ROW).toInt()
        }

        override fun createFragment(position: Int) = AudioPageFrag(position, requireViewModel())
    }
}