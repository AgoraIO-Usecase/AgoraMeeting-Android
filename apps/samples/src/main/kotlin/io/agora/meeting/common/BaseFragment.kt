package io.agora.meeting.common

import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

open class BaseFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    protected fun showAlert(message: String?) {
        val context = context ?: return
        AlertDialog.Builder(context).setTitle("Tips").setMessage(message)
                .setPositiveButton("OK") { dialog: DialogInterface, which: Int -> dialog.dismiss() }
                .show()
    }

    protected fun showLongToast(msg: String?) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
    }
}