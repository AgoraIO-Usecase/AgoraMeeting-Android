package io.agora.meeting.ui.module.users

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import io.agora.meeting.context.bean.UserDetailInfo
import io.agora.meeting.context.bean.UserOperation
import io.agora.meeting.context.bean.UserRole
import io.agora.meeting.ui.R
import io.agora.meeting.ui.base.BaseUiController
import io.agora.meeting.ui.base.BindingViewHolder
import io.agora.meeting.ui.databinding.UsersListItemBinding
import io.agora.meeting.ui.databinding.UsersListLayoutBinding
import io.agora.meeting.ui.fragment.ActionSheetFragment
import io.agora.meeting.ui.util.AvatarUtil

class UsersUC : BaseUiController<UsersListLayoutBinding, UsersVM>(UsersListLayoutBinding::class.java, UsersVM::class.java) {

    private val mAdapter by lazy { UserListItemAdapter() }

    override fun onViewCreated() {
        super.onViewCreated()
        initListView()
    }

    override fun onViewModelBind() {
        super.onViewModelBind()
        requireViewModel().userInfoList.observe(requireLifecycleOwner()) {
            mAdapter.submitList(it)
        }
    }

    private fun initListView() {
        requireBinding().apply {
            list.addItemDecoration(object : ItemDecoration() {
                override fun getItemOffsets(outRect: Rect, view: View,
                                            parent: RecyclerView,
                                            state: RecyclerView.State) {
                    val layoutParams = view.layoutParams
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    view.layoutParams = layoutParams
                    super.getItemOffsets(outRect, view, parent, state)
                }
            })
            val dividerItemDecoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
            dividerItemDecoration.setDrawable(requireContext().getDrawable(R.drawable.ic_divider)!!)
            list.addItemDecoration(dividerItemDecoration)
            list.adapter = mAdapter
            refreshLayout.setOnRefreshListener {
                val members = viewModel?.userInfoList?.value
                if (members != null) {
                    mAdapter.submitList(members)
                }
                refreshLayout.isRefreshing = false
            }
        }
    }


    private fun showUserOptionDialog(userInfo: UserDetailInfo) {
        val actionSheet = ActionSheetFragment.getInstance(userInfo.userName, userInfo.userId, R.menu.member_control)
        actionSheet.showMenu(userInfo.options.map {
            when (it) {
                UserOperation.AbandonHost -> R.id.menu_renounce_host
                UserOperation.BeHost -> R.id.menu_become_host
                UserOperation.SetAsHost -> R.id.menu_set_host
                UserOperation.CloseCamera -> R.id.menu_video
                UserOperation.CloseMic -> R.id.menu_mic
                else -> R.id.menu_move_out
            }
        })
        actionSheet.setOnItemClickListener { _: View?, _: Int, id: Long ->
            run {
                requireViewModel().handleUserOperation(userInfo, when (id.toInt()) {
                    R.id.menu_renounce_host -> UserOperation.AbandonHost
                    R.id.menu_become_host -> UserOperation.BeHost
                    R.id.menu_set_host -> UserOperation.SetAsHost
                    R.id.menu_video -> UserOperation.CloseCamera
                    R.id.menu_mic -> UserOperation.CloseMic
                    else -> UserOperation.KickOut
                })
            }
        }
        actionSheet.show(requireFragmentManager(), null)
    }

    fun setUserNameFilter(filter: String) {
        requireViewModel().setFilterName(filter)
    }


    inner class UserListItemAdapter : ListAdapter<UserDetailInfo, BindingViewHolder<UsersListItemBinding>>(UserDiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                BindingViewHolder(UsersListItemBinding.inflate(LayoutInflater.from(parent.context)))

        override fun onBindViewHolder(holder: BindingViewHolder<UsersListItemBinding>, position: Int) {
            val item = getItem(position)
            holder.binding.item = item
            holder.binding.container.setOnClickListener {
                showUserOptionDialog(item)
            }
            holder.binding.executePendingBindings()

            AvatarUtil.loadCircleAvatar(holder.binding.root, holder.binding.ivAvatar, item.userName)

            if (item.userRole == UserRole.host && item.isMe) {
                holder.binding.tvName.text = requireContext().resources.getString(R.string.member_list_host_me, item.userName)
            } else if (item.userRole == UserRole.host) {
                holder.binding.tvName.text = requireContext().resources.getString(R.string.member_list_host, item.userName)
            } else if (item.isMe) {
                holder.binding.tvName.text = requireContext().resources.getString(R.string.member_list_me, item.userName)
            }
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<UserDetailInfo>() {
        override fun areItemsTheSame(oldItem: UserDetailInfo, newItem: UserDetailInfo) = oldItem.userId == newItem.userId

        override fun areContentsTheSame(oldItem: UserDetailInfo, newItem: UserDetailInfo) =
                oldItem.userName == newItem.userName
                        && oldItem.userRole == newItem.userRole
                        && oldItem.options == newItem.options
                        && oldItem.hasAudio == newItem.hasAudio
                        && oldItem.hasVideo == newItem.hasVideo
                        && oldItem.isScreenSharing == newItem.isScreenSharing
                        && oldItem.isBoardSharing == newItem.isBoardSharing
    }

}