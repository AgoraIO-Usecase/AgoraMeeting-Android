package io.agora.meeting.ui.framework

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.Navigation
import io.agora.meeting.ui.R
import io.agora.meeting.ui.adapter.BindingAdapters
import io.agora.meeting.ui.base.AppBarDelegate
import io.agora.meeting.ui.module.root.RootUC

class RootActivity : AppCompatActivity(),
        AppBarDelegate,
        MainFragment.MainNavigation,
        NotifyFragment.NotifyNavigation,
        BoardFragment.BoardNavigation,
        MessageFragment.MessageNavigation,
        SimpleWebFragment.SimpleWebNavigation
{

    private var lastNavigateTime: Long = 0
    private val rootUC by lazy {
        RootUC().apply {
            onFinishListener = { finish() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.root_activity)
        setStatusBarTransparent()
        window.decorView.keepScreenOn = true

        rootUC.createViewBinding(this, this, window.decorView, false)
        rootUC.bindViewModel(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        rootUC.destroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navigateUp() || super.onSupportNavigateUp()
    }

    private fun getNavLayout(): Int {
        return R.id.nav_host_fragment
    }

    private fun navigateToPageSafely(view: View?, @IdRes targetId: Int, bundle: Bundle? = null) {
        // 两次点击间隔不能小于1s
        if (System.currentTimeMillis() - lastNavigateTime < 1000) {
            return
        }
        lastNavigateTime = System.currentTimeMillis()
        try {
            Navigation.findNavController(view!!).navigate(targetId, bundle)
        } catch (e: Exception) {
            try {
                Navigation.findNavController(this, getNavLayout()).navigate(targetId, bundle)
            } catch (e1: Exception) {
                e1.printStackTrace()
            }
        }
    }

    override fun setupAppBar(toolbar: Toolbar?, isLight: Boolean, title: String?) {
        BindingAdapters.bindToolbarTitle(toolbar, true, title, resources.getColor(R.color.global_text_color_black), resources.getDimensionPixelOffset(R.dimen.global_text_size_large).toFloat())
        setStatusBarStyle(isLight)
        setSupportActionBar(toolbar)
    }

    private fun setStatusBarStyle(isLight: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val window = window
            if (isLight) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            } else {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    private fun setStatusBarTransparent() {
        val window = window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.statusBarColor = Color.TRANSPARENT
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }

    override fun navigateUp(): Boolean {
        val navController = Navigation.findNavController(this, getNavLayout())
        return navController.navigateUp()
    }

    override fun navigateToUsersPage(view: View?) {
        navigateToPageSafely(view, R.id.action_to_usersFragment)
    }

    override fun navigateToSettingPage(view: View?) {
        navigateToPageSafely(view, R.id.action_to_settingFragment)
    }

    override fun navigateToMessagePage(view: View?) {
        navigateToPageSafely(view, R.id.action_to_messageFragment)
    }

    override fun navigateToWhiteboardPage(view: View?) {
        navigateToPageSafely(view, R.id.action_to_boardFragment)
    }

}