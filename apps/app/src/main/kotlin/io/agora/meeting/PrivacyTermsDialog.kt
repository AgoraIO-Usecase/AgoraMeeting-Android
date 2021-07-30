package io.agora.meeting

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.Rect
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.webkit.WebView
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import java.util.*

class PrivacyTermsDialog(private val mContext: Context) {
    private var checkBox: CheckBox? = null
    private var frameLayout: View? = null
    private var mDialog: AlertDialog? = null
    private var mDialogListener: OnPrivacyTermsDialogListener? = null
    private fun initView() {
        val customView = LayoutInflater.from(mContext).inflate(R.layout.dialog_privacy_terms, null)
        checkBox = customView.findViewById(R.id.termsCheck)
        initWebView(customView.findViewById(R.id.webview))
        frameLayout = customView

        if (mContext is Activity) {
            val rect = Rect()
            mContext.window.decorView.getGlobalVisibleRect(rect)
            if(rect.width() > rect.height()){
                frameLayout = FrameLayout(mContext).apply {
                    addView(customView, FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, rect.height() / 3))
                }
            }
        }
    }

    private fun initWebView(webView: WebView) {
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webView.loadUrl(String.format(Locale.US, "file:android_asset/privacy_%s.html", localLanguage))
    }

    private val localLanguage: String
        get() {
            val locale = Locale.getDefault()
            return if (!Locale.SIMPLIFIED_CHINESE.language.equals(locale.language, ignoreCase = true)) {
                "en"
            } else "cn"
        }

    fun show() {
        initView()
        val mDialogBuilder = AlertDialog.Builder(mContext)
                .setMessage(R.string.setting_title_user_agreement)
                .setOnDismissListener { clearUI() }
                .setView(frameLayout)
                .setCancelable(false)
                .setPositiveButton(R.string.accept) { dialog: DialogInterface?, which: Int ->
                    if (mDialogListener != null) {
                        mDialogListener!!.onPositiveClick()
                    }
                }
                .setNegativeButton(R.string.decline) { dialog: DialogInterface?, which: Int ->
                    if (mDialogListener != null) {
                        mDialogListener!!.onNegativeClick()
                    }
                }
        val dialog = mDialogBuilder.create()
        mDialog = dialog
        dialog.show()


        val positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        positive.isEnabled = false
        negative.setTextColor(ContextCompat.getColor(mContext, R.color.design_default_color_error))
        checkBox!!.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean -> positive.isEnabled = isChecked }
        checkBox!!.requestFocus()
    }

    private fun clearUI() {
        var parent: ViewParent? = null
        if (frameLayout != null) {
            parent = frameLayout!!.parent
        }
        if (parent is ViewGroup) {
            parent.removeAllViews()
        }
    }

    fun dismiss() {
        val dialog = mDialog
        dialog?.dismiss()
    }

    fun setPrivacyTermsDialogListener(listener: OnPrivacyTermsDialogListener?): PrivacyTermsDialog {
        mDialogListener = listener
        return this
    }

    interface OnPrivacyTermsDialogListener {
        fun onPositiveClick()
        fun onNegativeClick()
    }

    companion object {
        fun convertDpToPixel(context: Context, dp: Float): Int {
            val resources = context.resources
            val metrics = resources.displayMetrics
            return (dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
        }
    }

}