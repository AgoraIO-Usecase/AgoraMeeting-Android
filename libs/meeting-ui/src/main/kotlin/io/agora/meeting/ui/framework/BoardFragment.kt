package io.agora.meeting.ui.framework

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.lifecycle.observe
import io.agora.meeting.context.bean.BoardPermission
import io.agora.meeting.ui.R
import io.agora.meeting.ui.base.KBaseFragment
import io.agora.meeting.ui.module.board.BoardUC

class BoardFragment : KBaseFragment() {

    private var navigation: BoardNavigation? = null

    private val boardUC by lazy {
        createUiController(BoardUC::class.java).apply {
            onCloseListener = { navigation?.navigateUp() ?: requireActivity().finish() }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.board_fragment, container, false).apply {
            boardUC.createViewBinding(requireContext(), viewLifecycleOwner, findViewById(R.id.container), true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAppBar(requireView().findViewById(R.id.toolbar), false, getString(R.string.whiteboard_title))
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_whiteboard, menu)
        boardUC.bindViewModel(this).apply {
            boardPermission.observe(viewLifecycleOwner) {
                setMenuVisible(menu, R.id.menu_close_board, it == BoardPermission.Admin)
                setMenuVisible(menu, R.id.menu_apply_board, it == BoardPermission.Audience)
                setMenuVisible(menu, R.id.menu_quit_board, it == BoardPermission.Interactor)
            }
        }
    }

    private fun setMenuVisible(menu: Menu, menuId: Int, visible: Boolean) {
        menu.findItem(menuId)?.isVisible = visible
    }

    fun setNavigation(nav: BoardNavigation){
        navigation = nav
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_close_board -> {
                boardUC.closeBoard()
                return true
            }
            R.id.menu_apply_board -> {
                boardUC.applyBoard()
                return true
            }
            R.id.menu_quit_board -> {
                boardUC.quitBoard()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is BoardNavigation){
            navigation = context
        }
    }

    override fun onDetach() {
        if(navigation == requireActivity()){
            navigation = null
        }
        super.onDetach()
    }

    interface BoardNavigation {
        fun navigateUp(): Boolean
    }
}