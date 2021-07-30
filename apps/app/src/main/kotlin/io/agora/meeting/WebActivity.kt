package io.agora.meeting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import io.agora.meeting.ui.adapter.BindingAdapters
import io.agora.meeting.ui.base.AppBarDelegate
import io.agora.meeting.ui.framework.SimpleWebFragment

class WebActivity: AppCompatActivity(), AppBarDelegate {

    companion object {
        private const val EXTRA_URL = "url"

        fun launch(context: Context, url: String){
            context.startActivity(Intent(context, WebActivity::class.java).apply {
                putExtra(EXTRA_URL, url)
            })
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_Layout, SimpleWebFragment.newInstance(intent.getStringExtra(EXTRA_URL)!!))
                .commit()
    }

    override fun setupAppBar(toolbar: Toolbar?, isLight: Boolean, title: String?) {
        BindingAdapters.bindToolbarTitle(toolbar, true, title, resources.getColor(io.agora.meeting.ui.R.color.global_text_color_black), resources.getDimensionPixelOffset(io.agora.meeting.ui.R.dimen.global_text_size_large).toFloat())
        setSupportActionBar(toolbar)
    }

}