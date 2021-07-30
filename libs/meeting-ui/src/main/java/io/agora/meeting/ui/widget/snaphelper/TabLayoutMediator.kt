package io.agora.meeting.ui.widget.snaphelper

import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import java.lang.ref.WeakReference

class TabLayoutMediator(private val tabLayout: TabLayout,
                        private val recyclerView: RecyclerView,
                        private val pageColumn: Int,
                        private val tabConfigurationStrategy: TabConfigurationStrategy? = null
) {

    private var attached = false

    private var adapter: RecyclerView.Adapter<*>? = null
    private var onScrollListener: TabLayoutOnScrollListener? = null
    private var pagerAdapterObserver: PagerAdapterObserver? = null


    companion object {

        internal fun getCurrentItem(recyclerView: RecyclerView, pageColumn: Int): Int {
            val layoutManager = recyclerView.layoutManager

            var position = 0
            if (layoutManager is GridLayoutManager) {
                val gridLayoutManager = layoutManager
                position = gridLayoutManager.findFirstVisibleItemPosition()
                val row = gridLayoutManager.spanCount
                position = position / (row * pageColumn)
            } else if (layoutManager is LinearLayoutManager) {
                position = layoutManager.findFirstVisibleItemPosition()
            }
            return position
        }

    }

    fun attach() {
        check(!attached) { "TabLayoutMediator is already attached" }

        adapter = recyclerView.adapter
        checkNotNull(adapter) { "TabLayoutMediator attached before RecyclerView has an " + "adapter" }

        attached = true

        onScrollListener = TabLayoutOnScrollListener(tabLayout, pageColumn)
        recyclerView.addOnScrollListener(onScrollListener!!)

        pagerAdapterObserver = PagerAdapterObserver()
        adapter!!.registerAdapterDataObserver(pagerAdapterObserver!!)

        populateTabsFromPagerAdapter()

        tabLayout.setScrollPosition(getCurrentItemInner(), 0f, true)
    }

    fun detach() {
        onScrollListener?.let {
            recyclerView.removeOnScrollListener(it)
            onScrollListener = null
        }
        pagerAdapterObserver?.let {
            adapter?.unregisterAdapterDataObserver(it)
            pagerAdapterObserver = null
        }
        adapter = null
        attached = false
    }

    private fun getCurrentItemInner() = getCurrentItem(recyclerView, pageColumn)

    private fun getPageCount(): Int {
        val _adapter = adapter ?: return 0
        val itemCount: Int = _adapter.getItemCount()
        val eachPageCount: Int = eachPageItemCount()
        if (eachPageCount <= 0) return 0
        return if (itemCount % eachPageCount == 0) itemCount / eachPageCount else itemCount / eachPageCount + 1
    }

    private fun eachPageItemCount(): Int {
        var row = 1
        val layoutManager = recyclerView.getLayoutManager()
        if (layoutManager != null) {
            if (layoutManager is GridLayoutManager) {
                row = layoutManager.spanCount
            }
        }
        return row * pageColumn
    }

    private fun populateTabsFromPagerAdapter() {
        tabLayout.removeAllTabs()

        if (adapter != null) {
            val pageCount = getPageCount()
            for (i in 0 until pageCount) {
                val tab = tabLayout.newTab()
                tab.view.setOnTouchListener({ view: View, motionEvent: MotionEvent -> true})
                tabConfigurationStrategy?.onConfigureTab(tab, i, pageCount)
                tabLayout.addTab(tab, false)
            }
            // Make sure we reflect the currently set ViewPager item
            if (pageCount > 0) {
                val lastItem = tabLayout.tabCount - 1
                val currItem = Math.min(getCurrentItemInner(), lastItem)
                if (currItem != tabLayout.selectedTabPosition) {
                    tabLayout.selectTab(tabLayout.getTabAt(currItem))
                }
            }
        }
    }

    inner class PagerAdapterObserver : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            super.onChanged()
            populateTabsFromPagerAdapter()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            populateTabsFromPagerAdapter()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            populateTabsFromPagerAdapter()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            populateTabsFromPagerAdapter()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            populateTabsFromPagerAdapter()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            populateTabsFromPagerAdapter()
        }
    }

    class TabLayoutOnScrollListener(tabLayout: TabLayout, val pageColumn: Int) : RecyclerView.OnScrollListener() {

        private val tabLayoutRef = WeakReference(tabLayout)

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if(newState == RecyclerView.SCROLL_STATE_IDLE){
                val tabLayout = tabLayoutRef.get()
                val position = getCurrentItem(recyclerView, pageColumn)
                if (tabLayout != null && position < tabLayout.tabCount) {
                    tabLayout.selectTab(tabLayout.getTabAt(position), true)
                }
            }
        }
    }

    interface TabConfigurationStrategy {
        fun onConfigureTab(tab: TabLayout.Tab, position: Int, total: Int)
    }
}