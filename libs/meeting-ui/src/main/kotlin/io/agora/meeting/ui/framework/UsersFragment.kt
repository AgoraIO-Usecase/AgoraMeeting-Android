package io.agora.meeting.ui.framework

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.observe
import io.agora.meeting.ui.R
import io.agora.meeting.ui.base.KBaseFragment
import io.agora.meeting.ui.module.users.UsersUC

class UsersFragment : KBaseFragment() {

    private val usersUC by lazy { createUiController(UsersUC::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.users_fragment, container, false).apply {
            usersUC.createViewBinding(requireContext(), viewLifecycleOwner, findViewById(R.id.list_container))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolBar()

        usersUC.bindViewModel(this).apply {
            memberNum.observe(viewLifecycleOwner) {
                requireView().findViewById<Toolbar>(R.id.toolbar)?.title = getString(R.string.member_list_title, it)
            }
        }
    }

    private fun initToolBar(){
        setupAppBar(requireView().findViewById<Toolbar>(R.id.toolbar), false)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_member_list, menu)
        initSearchMenu(menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun initSearchMenu(menu: Menu) {
        val item = menu.findItem(R.id.action_search_kl)
        val searchView = item.actionView as SearchView
        searchView.queryHint = getString(R.string.member_list_search_hint)
        searchView.setOnCloseListener {
            usersUC.setUserNameFilter("")
            true
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                usersUC.setUserNameFilter(newText)
                return true
            }
        })
    }
}