package io.agora.meeting

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import io.agora.meeting.databinding.LayoutAboutBinding
import io.agora.meeting.sdk.MeetingSDK
import java.util.*

class AboutActivity : AppCompatActivity() {

    private val aboutLayoutBinding by lazy {
        LayoutAboutBinding.inflate(LayoutInflater.from(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        findViewById<FrameLayout>(R.id.fragment_Layout).addView(aboutLayoutBinding.root)

        aboutLayoutBinding.tvTeams.setOnClickListener {
            // 服务条款
            jumpToWeb(getString(R.string.teams_url, getLocalLanguage()))
        }
        aboutLayoutBinding.tvPolicy.setOnClickListener {
            jumpToWeb(getString(R.string.proxy_url, getLocalLanguage()))
        }
        aboutLayoutBinding.tvProductDisclaimer.setOnClickListener {
            // 免费声明
            val accessUrl = String.format(Locale.US, "file:android_asset/disclaimer_%s.html", getLocalLanguage())
            jumpToWeb(accessUrl)
        }
        aboutLayoutBinding.btnRegister.setOnClickListener {
            jumpToBrowser(getString(R.string.sign_up_url, getLocalLanguage()))
        }
        aboutLayoutBinding.btnDocument.setOnClickListener {
            jumpToBrowser(getString(R.string.document_url, getLocalLanguage()))
        }
        aboutLayoutBinding.tvTips.text = getString(R.string.about_version_tips_all,
                MeetingSDK.CORE_VERSION_NAME,
                BuildConfig.BUILD_TIME,
                MeetingSDK.CORE_RTC_VERSION,
                MeetingSDK.CORE_RTM_VERSION,
                MeetingSDK.CORE_WHITEBOARD_VERSION)

    }

    private fun jumpToBrowser(url: String) {
        startActivity(Intent.parseUri(url, Intent.URI_INTENT_SCHEME))
    }

    private fun jumpToWeb(url: String) {
        WebActivity.launch(this, url)
    }

    private fun getLocalLanguage(): String {
        val locale = Locale.getDefault()
        return if (!Locale.SIMPLIFIED_CHINESE.language.equals(locale.language, ignoreCase = true)) {
            "en"
        } else "cn"
    }
}