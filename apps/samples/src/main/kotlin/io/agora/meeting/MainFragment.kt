package io.agora.meeting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.meeting.common.BaseFragment
import io.agora.meeting.common.adapter.ExampleSection
import io.agora.meeting.common.model.Examples
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter

class MainFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            view.layoutManager = LinearLayoutManager(context)
            val sectionedAdapter = SectionedRecyclerViewAdapter()
            sectionedAdapter.addSection(ExampleSection(Examples.GROUP_FRAGMENT, Examples.ITEM_MAP[Examples.GROUP_FRAGMENT], requireActivity() as? ExampleSection.ItemClickListener))
            sectionedAdapter.addSection(ExampleSection(Examples.GROUP_UI_CONTROLLER, Examples.ITEM_MAP[Examples.GROUP_UI_CONTROLLER], requireActivity() as? ExampleSection.ItemClickListener))
            view.adapter = sectionedAdapter
        }

        return view
    }

}