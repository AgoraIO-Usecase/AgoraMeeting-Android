package io.agora.meeting.ui.module.main.render.tiled

import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.observe
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.agora.meeting.context.bean.RenderInfo
import io.agora.meeting.ui.base.BaseUiController
import io.agora.meeting.ui.databinding.MainRenderTiledVpContainerBinding
import kotlin.math.ceil

class TiledVPLayoutUC : BaseUiController<MainRenderTiledVpContainerBinding, TiledLayoutVM>(
        MainRenderTiledVpContainerBinding::class.java,
        TiledLayoutVM::class.java
) {

    private var adapter: TiledPageAdapter? = null

    var onItemClickListener: ((View?, RenderInfo) -> Unit)? = null

    // true: long click of video will display the stats info layout
    // false: hide the stats info layout
    var statsDisplayEnable = false
        set(value) {
            field = value
            viewModel?.statsDisplayEnable = value
        }

    override fun onViewCreated() {
        super.onViewCreated()
        adapter = TiledPageAdapter()
        requireBinding().viewpager2.offscreenPageLimit = 1
        requireBinding().viewpager2.adapter = adapter

        TabLayoutMediator(requireBinding().pageTabLayout, requireBinding().viewpager2, object : TabLayoutMediator.TabConfigurationStrategy {
            override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
                binding ?: return
                val total = requireBinding().viewpager2.adapter?.itemCount ?: 0
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
        requireBinding().viewpager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            private var lastPositionOffset = 0f
            private var lastPosition = 0

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)


                val diff = positionOffset - lastPositionOffset
                lastPositionOffset = positionOffset

                var nextPosition = 0
                var currPosition = 0
                var nextVisiblePercent = 0f
                var currVisiblePercent = 0f
                if (diff >= 0) {
                    nextPosition = position + 1
                    currPosition = position
                    nextVisiblePercent = positionOffset
                    currVisiblePercent = 1 - positionOffset
                } else {
                    nextPosition = position
                    currPosition = position + 1
                    nextVisiblePercent = 1 - positionOffset
                    currVisiblePercent = positionOffset
                }

                if(position > lastPosition){
                    nextPosition = lastPosition
                    currPosition = position
                    nextVisiblePercent = positionOffset
                    currVisiblePercent = 1 - positionOffset
                }

                lastPosition = position

                val currFrag = requireFragmentManager().fragments.find { (it as? TiledVPPageFrag)?.index == currPosition } as? TiledVPPageFrag
                val nextFrag = requireFragmentManager().fragments.find { (it as? TiledVPPageFrag)?.index == nextPosition } as? TiledVPPageFrag

                currFrag?.onVisibleRectChanged(currVisiblePercent)
                nextFrag?.onVisibleRectChanged(nextVisiblePercent)

            }
        })
    }

    override fun onViewModelBind() {
        super.onViewModelBind()
        requireViewModel().statsDisplayEnable = statsDisplayEnable
        requireViewModel().renderList.observe(requireLifecycleOwner()) {
            adapter?.notifyDataSetChanged()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter = null
    }

    inner class TiledPageAdapter : FragmentStateAdapter(requireFragmentManager(), requireLifecycleOwner().lifecycle) {
        override fun getItemCount(): Int {
            val listSize = viewModel?.renderList?.value?.size ?: 0
            return ceil(listSize * 1.0f / TiledVPPageFrag.PAGE_COLUMN / TiledVPPageFrag.PAGE_ROW).toInt()
        }

        override fun createFragment(position: Int) = TiledVPPageFrag(position, requireViewModel(), onItemClickListener)
    }

}