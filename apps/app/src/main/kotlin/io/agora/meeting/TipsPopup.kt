package io.agora.meeting

import android.content.Context
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import razerdp.basepopup.BasePopupWindow

class TipsPopup : BasePopupWindow {
    private lateinit var iv_arrow: ImageView

    constructor(context: Context?) : super(context) {}
    constructor(fragment: Fragment?) : super(fragment) {}

    override fun onCreateContentView(): View {
        val view = createPopupById(R.layout.layout_popup)
        iv_arrow = view.findViewById(R.id.iv_arrow)
        return view
    }

    override fun onPopupLayout(popupRect: Rect, anchorRect: Rect) {
        val gravity = computeGravity(popupRect, anchorRect)
        var verticalCenter = false
        when (gravity and Gravity.VERTICAL_GRAVITY_MASK) {
            Gravity.TOP -> {
                iv_arrow.visibility = View.VISIBLE
                iv_arrow.translationX = (popupRect.width() - iv_arrow.width shr 1.toFloat().toInt()).toFloat()
                iv_arrow.translationY = popupRect.height() - iv_arrow.height.toFloat()
                iv_arrow.rotation = 0f
            }
            Gravity.BOTTOM -> {
                iv_arrow.visibility = View.VISIBLE
                iv_arrow.translationX = (popupRect.width() - iv_arrow.width shr 1.toFloat().toInt()).toFloat()
                iv_arrow.translationY = 0f
                iv_arrow.rotation = 180f
            }
            Gravity.CENTER_VERTICAL -> verticalCenter = true
        }
        when (gravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
            Gravity.LEFT -> {
                iv_arrow.visibility = View.VISIBLE
                iv_arrow.x = popupRect.width() / 2 + anchorRect.centerX() - popupRect.centerX() - iv_arrow.width.toFloat()
            }
            Gravity.RIGHT -> {
                iv_arrow.visibility = View.VISIBLE
                iv_arrow.translationX = 0f
            }
            Gravity.CENTER_HORIZONTAL -> iv_arrow.visibility = if (verticalCenter) View.INVISIBLE else View.VISIBLE
        }
    }
}