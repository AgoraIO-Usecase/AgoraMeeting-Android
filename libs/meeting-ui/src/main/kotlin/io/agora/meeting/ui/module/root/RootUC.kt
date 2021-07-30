package io.agora.meeting.ui.module.root

import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.observe
import io.agora.meeting.context.bean.RoomClosedReason
import io.agora.meeting.ui.R
import io.agora.meeting.ui.base.BaseUiController
import io.agora.meeting.ui.databinding.RootLayoutEmptyBinding
import io.agora.meeting.ui.util.ToastUtil

class RootUC : BaseUiController<RootLayoutEmptyBinding, RootVM>(
        RootLayoutEmptyBinding::class.java,
        RootVM::class.java
){

    var onFinishListener : (()->Unit)? = null

    override fun onViewModelBind() {
        super.onViewModelBind()
        requireViewModel().apply {
            roomJoinFailEvent.observe(requireLifecycleOwner()){
                val failed = it.getContentIfNotHandled() ?: return@observe
                if (failed) {
                    ToastUtil.showLong(R.string.main_join_failed)
                    onFinishListener?.invoke()
                }
            }
            roomCloseEvent.observe(requireLifecycleOwner()){
                val reason = it.getContentIfNotHandled() ?: return@observe
                if(reason == RoomClosedReason.EndByMySelf){
                    onFinishListener?.invoke()
                }else{
                    showForceExitDialog(R.string.main_close_title)
                }
            }
            kickOutEvent.observe(requireLifecycleOwner()){
                it.getContentIfNotHandled() ?: return@observe
                showForceExitDialog(R.string.main_removed_from_room)
            }
        }
    }

    private fun showForceExitDialog(@StringRes titleRes: Int) {
        AlertDialog.Builder(requireContext())
                .setMessage(titleRes)
                .setPositiveButton(R.string.cmm_know) { dialog, which ->
                    if (titleRes == R.string.main_close_title) {
                        onFinishListener?.invoke()
                    } else if (titleRes == R.string.main_removed_from_room) {
                        onFinishListener?.invoke()
                    }
                }.setCancelable(false).show()
    }


}