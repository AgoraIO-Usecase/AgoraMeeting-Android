package io.agora.meeting.ui.module.main.top

import android.content.DialogInterface
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.observe
import io.agora.meeting.context.bean.AudioRoute
import io.agora.meeting.ui.R
import io.agora.meeting.ui.base.BaseUiController
import io.agora.meeting.ui.databinding.MainTopLayoutBinding
import io.agora.meeting.ui.fragment.ActionSheetFragment
import io.agora.meeting.ui.util.ClipboardUtil
import io.agora.meeting.ui.util.ShareUtils
import io.agora.meeting.ui.util.ToastUtil

class MainTopUC : BaseUiController<MainTopLayoutBinding, MainTopVM>(MainTopLayoutBinding::class.java, MainTopVM::class.java) {

    override fun onViewModelBind() {
        super.onViewModelBind()
        requireViewModel().audioRouteIcon.observe(requireLifecycleOwner()) {
            // change audio icon
            requireBinding().audioSwitch.setImageResource(
                    when (it) {
                        AudioRoute.HEADSET -> R.drawable.ic_headset
                        AudioRoute.HEADSETNOMIC -> R.drawable.ic_headset
                        AudioRoute.HEADSETBLUETOOTH -> R.drawable.ic_headset
                        AudioRoute.EARPIECE -> R.drawable.ic_speaker_off
                        AudioRoute.SPEAKER -> R.drawable.ic_speaker_on
                    }
            )
        }
        requireViewModel().leaveClickEvent.observe(requireLifecycleOwner()) { canCloseEvent ->
            canCloseEvent.getContentIfNotHandled() ?: return@observe
            // show leave dialog
            showLeaveDialog()
        }
        requireViewModel().copyRoomEvent.observe(requireLifecycleOwner()) {
            val copyInfo = it.getContentIfNotHandled() ?: return@observe

            ClipboardUtil.copy2Clipboard(requireContext(), ShareUtils.getMeetingShareInfo(requireContext(), copyInfo.roomInfo, copyInfo.userInfo))
            ToastUtil.showShort(requireContext().getString(R.string.invite_meeting_info_copy_success))
        }
    }

    fun showLeaveDialog() {
        if (requireViewModel().isScreenSharingByMe()) {
            AlertDialog.Builder(requireContext())
                    .setMessage(R.string.notify_popup_leave_with_screenshare_on)
                    .setNegativeButton(R.string.cmm_no) { dialogInterface: DialogInterface, _: Int ->
                        dialogInterface.dismiss()
                    }
                    .setPositiveButton(R.string.cmm_yes) { dialogInterface: DialogInterface, _: Int ->
                        requireViewModel().stopScreenSharing()
                        dialogInterface.dismiss()
                    }
                    .show()
            return
        }

        if (requireViewModel().isBoardSharingByMe()) {
            AlertDialog.Builder(requireContext())
                    .setMessage(R.string.notify_popup_leave_with_whiteboard_on)
                    .setNegativeButton(R.string.cmm_no) { dialogInterface: DialogInterface, _: Int ->
                        dialogInterface.dismiss()
                    }
                    .setPositiveButton(R.string.cmm_yes) { dialogInterface: DialogInterface, _: Int ->
                        requireViewModel().stopBoardSharing()
                        dialogInterface.dismiss()
                    }
                    .show()
            return
        }

        ActionSheetFragment.getInstance(R.menu.sheet_meeting_exit).let {
            it.setOnItemClickListener { _: View, _: Int, id: Long ->
                when (id.toInt()) {
                    R.id.menu_close_meeting -> requireViewModel().closeRoom()
                    R.id.menu_exist_meeting -> requireViewModel().leaveRoom()
                }
            }
            if (!requireViewModel().canCloseRoom()) {
                it.removeMenu(listOf(R.id.menu_close_meeting))
            }
            it.show(requireFragmentManager(), null)
        }
    }
}