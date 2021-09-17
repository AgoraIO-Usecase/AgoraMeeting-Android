package io.agora.meeting

import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.widget.RatingBar
import android.widget.RatingBar.OnRatingBarChangeListener
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceManager
import io.agora.meeting.context.OnExitListener
import io.agora.meeting.context.bean.*
import io.agora.meeting.databinding.LayoutRatingBinding
import io.agora.meeting.http.VersionCheck
import io.agora.meeting.ui.framework.MainFragmentArgs
import io.agora.meeting.ui.util.CryptoUtil
import io.agora.meeting.ui.util.KeyboardUtil
import io.agora.meeting.ui.util.ToastUtil
import io.agora.meeting.ui.util.UUIDUtil
import io.agora.meeting.ui.widget.AutoEditText
import io.agora.meeting.ui.widget.LoadingButton
import io.agora.rtmtoken.RtmTokenBuilder
import io.agora.scene.statistic.AgoraSceneStatistic
import io.agora.scene.statistic.AgoraSceneStatisticContext
import io.agora.scene.statistic.AgoraUserRatingValue
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.*


class LoginActivity : AppCompatActivity() {

    companion object {
        var sUserName = ""
    }

    private val loginBtn by lazy { findViewById<LoadingButton>(R.id.btn_enter) }
    private val roomNameAutoEditText by lazy { findViewById<AutoEditText>(R.id.aet_room_name) }
    private val roomPwdAutoEditText by lazy { findViewById<AutoEditText>(R.id.aet_room_pwd) }
    private val userNameAutoEditText by lazy { findViewById<AutoEditText>(R.id.aet_name) }
    private val cameraSwitch by lazy { findViewById<Switch>(R.id.sw_camera) }
    private val micSwitch by lazy { findViewById<Switch>(R.id.sw_mic) }
    private lateinit var signalMenu: MenuItem
    private val meetingSDK by lazy { MainApplication.getMeetingSDK(this) }

    private val versionCheck by lazy { VersionCheck(getString(R.string.agora_app_id), getString(R.string.agora_app_cert)) }
    private val sharePreference by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }
    private val agoraSceneStatistic by lazy {
        AgoraSceneStatistic(OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor(object: HttpLoggingInterceptor.Logger{
                    override fun log(message: String) {
                        Log.d("SceneStatistic", message)
                    }
                }).setLevel(HttpLoggingInterceptor.Level.BODY))
                .build())
    }

    private val onExitListener = object : OnExitListener {
        override fun onExit(saved: RoomCache, reason: ExistReason) {
            Log.d("LoginActivity", "onExitListener onExit saved=$saved, existReason=$reason")
            showRateDialog(saved)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initToolBar()
        initPrivacyDialog {
            checkVersion()
            loginBtn.setOnClickListener {
                KeyboardUtil.hideInput(this)
                launchMeetingSdk()
            }
        }
        roomPwdAutoEditText.setRightIconClickListener {
            KeyboardUtil.hideInput(this);
            TipsPopup(this)
                    .setBackgroundColor(Color.TRANSPARENT)
                    .setPopupGravity(Gravity.BOTTOM)
                    .showPopupWindow(it)
        }

        meetingSDK.setOnExitListener(onExitListener)
    }

    override fun onResume() {
        super.onResume()
        resetNetQualityCheck()
    }

    private fun showRateDialog(saved: RoomCache) {
        val binding: LayoutRatingBinding = LayoutRatingBinding.inflate(layoutInflater)
        bindRatingBarAndTv(binding.rbCallQuality, binding.tvCallQuality)
        bindRatingBarAndTv(binding.rbFunctionCompleteness, binding.tvFunctionCompleteness)
        bindRatingBarAndTv(binding.rbGeneralExperience, binding.tvGeneralExperience)

        val rect = Rect()
        window.decorView.getGlobalVisibleRect(rect)
        if(rect.width() > rect.height()){
            val layoutParams = binding.scrollview.layoutParams
            layoutParams.height = (rect.height() / 2.5).toInt()
            binding.scrollview.layoutParams = layoutParams
        }

        AlertDialog.Builder(this)
                .setMessage(R.string.rating_title)
                .setCancelable(true)
                .setView(binding.root)
                .setPositiveButton(R.string.cmm_submit) { _, _ ->
                    agoraSceneStatistic.setContent(AgoraSceneStatisticContext(applicationContext, saved.userInfo.userId, "meeting", saved.roomInfo.roomId))
                    agoraSceneStatistic.userRating(
                            AgoraUserRatingValue(
                                    binding.rbCallQuality.rating,
                                    binding.rbFunctionCompleteness.rating,
                                    binding.rbGeneralExperience.rating,
                            ),
                            binding.etComment.text.toString(),
                            {
                                Log.d("LoginActivity", "rate success savedConfig=$saved")
                            },
                            {
                                ToastUtil.showShort(R.string.net_poor)
                            }
                    )
                }
                .show()
    }

    private fun bindRatingBarAndTv(ratingBar: RatingBar, textView: TextView) {
        ratingBar.onRatingBarChangeListener = OnRatingBarChangeListener { ratingBar1: RatingBar?, rating: Float, fromUser: Boolean ->
            var _rating = rating
            if (_rating == 0f) {
                _rating = 1f
                ratingBar.rating = 1f
            }
            textView.text = getString(R.string.rating_start, _rating.toInt())
        }
        textView.text = getString(R.string.rating_start, ratingBar.rating.toInt())
    }

    private fun initPrivacyDialog(run: () -> Unit) {
        val showed = sharePreference.getBoolean(getString(R.string.key_show_privacy_terms), false)
        if (!showed) {
            val dialog = PrivacyTermsDialog(this)
                    .setPrivacyTermsDialogListener(object : PrivacyTermsDialog.OnPrivacyTermsDialogListener {
                        override fun onPositiveClick() {
                            run.invoke()
                            sharePreference.edit().putBoolean(getString(R.string.key_show_privacy_terms), true).apply()
                        }

                        override fun onNegativeClick() {
                            finish()
                        }

                    })
            window.decorView.post {
                dialog.show()
            }
        } else {
            run.invoke()
        }
    }

    private fun checkVersion() {
        versionCheck.check({
            AlertDialog.Builder(this@LoginActivity)
                    .setMessage(R.string.about_upgrade_title)
                    .setCancelable(false)
                    .setPositiveButton(R.string.cmm_update, null)
                    .show()
                    .apply {
                        getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { v ->
                            dismiss()
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.download_url, getLocalLanguage()))))
                        }
                    }
        }, {
            if (it is IllegalStateException) {
                ToastUtil.showShort(it.message)
            } else {
                ToastUtil.showShort(R.string.net_poor)
            }
        })
    }

    private fun getLocalLanguage(): String {
        val locale = Locale.getDefault()
        return if (!Locale.SIMPLIFIED_CHINESE.language.equals(locale.language, ignoreCase = true)) {
            "en"
        } else "cn"
    }

    private fun resetNetQualityCheck() {
        meetingSDK.enableNetQualityCheck({
            signalMenu.setIcon(
                    when (it) {
                        DeviceNetQuality.IDLE -> R.drawable.ic_signal_idle
                        DeviceNetQuality.GOOD -> R.drawable.ic_signal_good
                        DeviceNetQuality.POOR -> R.drawable.ic_signal_poor
                        DeviceNetQuality.BAD -> R.drawable.ic_signal_bad
                        DeviceNetQuality.DISCONNECT -> R.drawable.ic_signal_idle
                    }
            )
        }, {
            ToastUtil.showLong(it.message)
        })
    }

    private fun initToolBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.menu.findItem(R.id.action_to_settingFragment).setOnMenuItemClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
            true
        }
        signalMenu = toolbar.menu.findItem(R.id.menu_signal)
    }

    override fun onPause() {
        super.onPause()
        meetingSDK.disableNetQualityCheck()
        sUserName = userNameAutoEditText.text
    }

    private fun launchMeetingSdk() {
        if(!roomNameAutoEditText.check()){
           return
        }
        if(!userNameAutoEditText.check()){
           return
        }
        loginBtn.showLoading()
        val userId = UUIDUtil.getUUID()
        val roomId = CryptoUtil.md5(roomNameAutoEditText.text)!!
        meetingSDK.launch(
                LaunchConfig(
                        roomId,
                        roomNameAutoEditText.text,
                        roomPwdAutoEditText.text,
                        userId,
                        userNameAutoEditText.text,
                        RtmTokenBuilder().buildToken(getString(R.string.agora_app_id), getString(R.string.agora_app_cert), userId, RtmTokenBuilder.Role.Rtm_User, 0),
                        2700,
                        1000,
                        cameraSwitch.isChecked,
                        micSwitch.isChecked,
                        CameraDirection.Front,
                        PreferenceManager.getDefaultSharedPreferences(this).getInt(getString(R.string.key_notify_max_num), 50),
                        mapOf(Pair("roomId", roomId), Pair("userId", userId))
                ),
                {
                    loginBtn.showButtonText()
                },
                {
                    ToastUtil.showLong(it.message)
                    loginBtn.showButtonText()
                },
                extras = Bundle().apply {
                    putAll(MainFragmentArgs(true).toBundle())
                }
        )
    }

}