package io.agora.meeting

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import io.agora.meeting.common.Constant
import io.agora.meeting.common.model.ExampleBean
import io.agora.meeting.ui.adapter.BindingAdapters
import io.agora.meeting.ui.base.AppBarDelegate
import io.agora.meeting.ui.module.root.RootUC

class ExampleActivity : AppCompatActivity(), AppBarDelegate {

    companion object{

        fun launch(activity: FragmentActivity, exampleBean: ExampleBean) {
            activity.startActivity(Intent(activity, ExampleActivity::class.java).apply {
                putExtra(Constant.DATA, exampleBean)
            })
        }

    }

    private lateinit var  exampleBean: ExampleBean

    private val rootUC by lazy {
        RootUC().apply {
            onFinishListener = { finish() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example_layout)
        exampleBean = intent.getParcelableExtra(Constant.DATA)!!

        setSupportActionBar(findViewById(R.id.toolbar))

        supportActionBar?.apply {
            setTitle(exampleBean.nameStrId)
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        rootUC.createViewBinding(this, this, null)
        rootUC.bindViewModel(this)

        val fragment = exampleBean.clazz.newInstance() as Fragment

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_Layout, fragment)
                .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        rootUC.destroy()
    }

    override fun setupAppBar(toolbar: Toolbar?, isLight: Boolean, title: String?) {
        findViewById<Toolbar>(R.id.toolbar).visibility = View.GONE
        BindingAdapters.bindToolbarTitle(toolbar, true, title, resources.getColor(io.agora.meeting.ui.R.color.global_text_color_black), resources.getDimensionPixelOffset(io.agora.meeting.ui.R.dimen.global_text_size_large).toFloat())
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}