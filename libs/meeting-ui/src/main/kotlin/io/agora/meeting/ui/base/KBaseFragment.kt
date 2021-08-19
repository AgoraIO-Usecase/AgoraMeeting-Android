package io.agora.meeting.ui.base

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide

open class KBaseFragment : Fragment(), AppBarDelegate {
    private val uiControllers = ArrayList<BaseUiController<*, *>>()

    @Volatile
    private var isSelected = false

    @Volatile
    private var isVisibleToUser = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireView().addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                onUserVisibleChanged(true)
            }

            override fun onViewDetachedFromWindow(v: View) {
                onUserVisibleChanged(false)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            Glide.with(this).onDestroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        uiControllers.forEach {
            it.destroy()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        uiControllers.forEach {
            it.attachFragment = null
        }
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (java.lang.Boolean.compare(menuVisible, isSelected) != 0) {
            isSelected = menuVisible
            onSelectedChanged(isSelected)
        }
    }

    fun isSelected(): Boolean {
        return isSelected
    }

    fun isVisibleToUser() = isVisibleToUser

    protected fun <UC : BaseUiController<*, *>>
            createUiController(ucClass: Class<UC>) : UC {
        val constructor = ucClass.getConstructor()
        val newInstance = constructor.newInstance()
        newInstance.attachFragment = this
        uiControllers.add(newInstance)
        return newInstance
    }

    protected fun safeGetArguments(run: (Bundle) -> Unit) {
        val fragArguments = arguments
        if (fragArguments != null) {
            try {
                run.invoke(fragArguments)
                return
            } catch (e: Exception) {
                // do nothing
            }
        }
        val activityArguments = activity?.intent?.extras
        if (activityArguments != null) {
            try {
                run.invoke(activityArguments)
                return
            } catch (e: Exception) {
                // do nothing
            }
        }
    }

    protected open fun onUserVisibleChanged(visible: Boolean) {
        uiControllers.forEach { it.visibleToUser = visible }
        isVisibleToUser = visible
    }

    protected open fun onSelectedChanged(selected: Boolean) {
        uiControllers.forEach { it.visibleToUser = selected }
    }

    override fun setupAppBar(toolbar: Toolbar?, isLight: Boolean, title: String?) {
        (requireActivity() as? AppBarDelegate)?.setupAppBar(toolbar, isLight, title)
    }


}