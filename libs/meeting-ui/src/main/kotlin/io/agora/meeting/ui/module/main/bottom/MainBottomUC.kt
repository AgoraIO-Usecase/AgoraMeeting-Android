package io.agora.meeting.ui.module.main.bottom

import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.observe
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import io.agora.meeting.context.bean.DeviceType
import io.agora.meeting.context.bean.LocalDeviceState
import io.agora.meeting.context.bean.UserOperation
import io.agora.meeting.ui.R
import io.agora.meeting.ui.base.BaseUiController
import io.agora.meeting.ui.databinding.MainBottomLayoutBinding
import io.agora.meeting.ui.fragment.ActionSheetFragment
import io.agora.meeting.ui.util.ClipboardUtil
import io.agora.meeting.ui.util.ShareUtils
import io.agora.meeting.ui.util.ToastUtil
import io.agora.meeting.ui.widget.CountDownMenuView
import q.rorbin.badgeview.QBadgeView

class MainBottomUC : BaseUiController<MainBottomLayoutBinding, MainBottomVM>(MainBottomLayoutBinding::class.java, MainBottomVM::class.java) {

    private var mUserDialog: AlertDialog? = null

    private val qBadgeView by lazy { QBadgeView(requireContext()) }
    private val micCdView by lazy { CountDownMenuView(requireContext()) }
    private val videoCdView by lazy { CountDownMenuView(requireContext()) }
    private var mic: BottomNavigationItemView? = null
    private var video: BottomNavigationItemView? = null
    private var chat: BottomNavigationItemView? = null

    var usersClickListener: ((View?) -> Unit)? = null
    var chatClickListener: ((View?) -> Unit)? = null
    var settingClickListener: ((View?) -> Unit)? = null

    override fun onViewCreated() {
        super.onViewCreated()

        mic = requireBinding().root.findViewById(R.id.menu_mic)
        video = requireBinding().root.findViewById(R.id.menu_video)
        chat = requireBinding().root.findViewById<BottomNavigationItemView>(R.id.menu_chat)?.apply{
            val container = parent as ViewGroup
            val index = container.indexOfChild(this)
            val params = layoutParams

            container.removeViewAt(index)

            val wrapContainer = FrameLayout(container.context)
            wrapContainer.addView(this, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.MATCH_PARENT).apply {
                gravity = Gravity.CENTER
                val padding = wrapContainer.context.resources.getDimensionPixelOffset(R.dimen.meeting_bottom_chat_padding)
                setPadding(padding, 0, padding, 0)
            })
            container.addView(wrapContainer, index, params)
        }

        qBadgeView.bindTarget(chat)
        micCdView.setupTarget(mic)
        videoCdView.setupTarget(video)
        requireBinding().navView.setOnNavigationItemSelectedListener(this::onBottomMenuClicked)
    }


    override fun onViewModelBind() {
        super.onViewModelBind()
        requireViewModel().apply {
            cameraState.observe(requireLifecycleOwner()) {
                Log.d("MainBottomUC", "state=$it")
                when (it.state) {
                    LocalDeviceState.Approving -> {
                        videoCdView.isVisible = true
                        videoCdView.setSecondTv(it.leftSecond)
                    }
                    else -> {
                        videoCdView.isVisible = false
                        video?.isActivated = it.state == LocalDeviceState.Open
                    }
                }
            }
            micState.observe(requireLifecycleOwner()) {
                when (it.state) {
                    LocalDeviceState.Approving ->{
                        micCdView.isVisible = true
                        micCdView.setSecondTv(it.leftSecond)
                    }
                    else -> {
                        micCdView.isVisible = false
                        mic?.isActivated = it.state == LocalDeviceState.Open
                    }
                }
            }
            unReadMsgCount.observe(requireLifecycleOwner()) {
                if (it > 0) {
                    qBadgeView.badgeNumber = it
                } else {
                    qBadgeView.hide(false)
                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        mic = null
        video = null
        chat = null
    }


    private fun onBottomMenuClicked(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_mic -> onBottomMicClick()
            R.id.menu_video -> onBottomCameraClick()
            R.id.menu_member -> usersClickListener?.invoke(item.actionView)
            R.id.menu_chat -> {
                requireViewModel().cleanUnReadCount()
                chatClickListener?.invoke(item.actionView)
            }
            R.id.menu_more -> showActionSheet()
        }
        return true
    }


    private fun onBottomMicClick() {
        mUserDialog?.dismiss()
        val enable = !mic!!.isActivated
        if (enable && requireViewModel().checkMicApproveNeed()) {
            mUserDialog = AlertDialog.Builder(requireContext())
                    .setMessage(R.string.notify_popup_request_to_turn_mic_on)
                    .setPositiveButton(R.string.cmm_apply) { _, _ ->
                        requireViewModel().openLocalMicDevice()
                    }
                    .setNegativeButton(R.string.cmm_cancel) { dialog, _ -> dialog.dismiss() }
                    .show()
        } else {
            if (enable) {
                requireViewModel().openLocalMicDevice()
            } else {
                requireViewModel().closeLocalMicDevice()
            }
        }
    }

    private fun onBottomCameraClick() {
        mUserDialog?.dismiss()
        val enable = !video!!.isActivated
        if (enable && requireViewModel().checkCameraApproveNeed()) {
            mUserDialog = AlertDialog.Builder(requireContext())
                    .setMessage(R.string.notify_popup_request_to_turn_cam_on)
                    .setPositiveButton(R.string.cmm_apply) { _, _ ->
                        requireViewModel().openLocalCameraDevice()
                    }
                    .setNegativeButton(R.string.cmm_cancel) { dialog, _ -> dialog.dismiss() }
                    .show()
        } else {
            if (enable) {
                requireViewModel().openLocalCameraDevice()
            } else {
                requireViewModel().closeLocalCameraDevice()
            }
        }
    }

    private fun showActionSheet() {
        val localUserOptions = requireViewModel().getLocalUserOptions()
        val mActionSheetDialog = ActionSheetFragment.getInstance(R.menu.sheet_meeting_more)
        mActionSheetDialog.showMenu(mutableListOf<Int>().apply {
            add(R.id.menu_invite)
            addAll(localUserOptions.map {
                when (it) {
                    UserOperation.CloseAllCamera -> R.id.menu_mute_all_camera
                    UserOperation.CloseAllMic -> R.id.menu_mute_all_mic
                    else -> throw IllegalArgumentException()
                }
            })
            if (requireViewModel().isMyScreenSharing()) {
                add(R.id.menu_screen_end)
            } else {
                add(R.id.menu_screen_open)
            }
            add(R.id.menu_board)
            add(R.id.menu_setting)
        })
        mActionSheetDialog.setOnItemClickListener { view: View?, _: Int, id: Long ->
            when (id.toInt()) {
                R.id.menu_invite -> {
                    requireViewModel().getLocalUserInfo().let {
                        ClipboardUtil.copy2Clipboard(requireContext(), ShareUtils.getMeetingShareInfo(requireContext(), requireViewModel().getRoomInfo(), it))
                        ToastUtil.showShort(requireContext().getString(R.string.invite_meeting_info_copy_success))
                    }
                }
                R.id.menu_mute_all_mic -> {
                    val checkBox = CheckBox(requireContext()).apply {
                        setText(R.string.notify_popup_permission_needed_to_turn_mic_on)
                    }
                    mUserDialog = AlertDialog.Builder(requireContext())
                            .setView(checkBox)
                            .setMessage(R.string.more_mute_all_mic)
                            .setPositiveButton(R.string.cmm_continue) { dialog, _ ->
                                dialog.dismiss()
                                requireViewModel().dealLocalUserOperation(UserOperation.CloseAllMic)
                                if (checkBox.isChecked) {
                                    requireViewModel().changeUserPermission(DeviceType.Mic, false)
                                }
                            }
                            .setNegativeButton(R.string.cmm_cancel) { dialog, _ -> dialog.dismiss() }
                            .show()
                }
                R.id.menu_mute_all_camera -> {
                    val checkBox = CheckBox(requireContext()).apply {
                        setText(R.string.notify_popup_permission_needed_to_turn_camera_on)
                    }
                    mUserDialog = AlertDialog.Builder(requireContext())
                            .setView(checkBox)
                            .setMessage(R.string.more_mute_all_camera)
                            .setPositiveButton(R.string.cmm_continue) { dialog, _ ->
                                dialog.dismiss()
                                requireViewModel().dealLocalUserOperation(UserOperation.CloseAllCamera)
                                if (checkBox.isChecked) {
                                    requireViewModel().changeUserPermission(DeviceType.Camera, false)
                                }
                            }
                            .setNegativeButton(R.string.cmm_cancel) { dialog, _ -> dialog.dismiss() }
                            .show()
                }
                R.id.menu_board -> {
                    requireViewModel().openBoardSharing()
                }
                R.id.menu_screen_open -> {
                    mUserDialog = AlertDialog.Builder(requireContext())
                            .setMessage(R.string.notify_popup_start_screen_share_tip)
                            .setPositiveButton(R.string.notify_popup_start_sharing) { _, _ ->
                                run {
                                    requireViewModel().openScreenSharing()
                                }
                            }
                            .setNegativeButton(R.string.cmm_cancel) { dialog, _ -> dialog.dismiss() }
                            .show()
                }
                R.id.menu_screen_end -> {
                    requireViewModel().closeScreenSharing()
                }
                R.id.menu_setting -> {
                    settingClickListener?.invoke(view)
                }
            }
        }
        mActionSheetDialog.show(requireFragmentManager(), null)
    }
}