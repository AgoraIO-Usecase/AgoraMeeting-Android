package io.agora.meeting

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.agora.meeting.context.OnExitListener
import io.agora.meeting.context.bean.CameraDirection
import io.agora.meeting.context.bean.ExistReason
import io.agora.meeting.context.bean.LaunchConfig
import io.agora.meeting.context.bean.RoomCache
import io.agora.meeting.ui.util.CryptoUtil
import io.agora.meeting.ui.util.ToastUtil
import io.agora.meeting.ui.widget.LoadingButton
import io.agora.rtmtoken.RtmTokenBuilder


class LoginActivity : AppCompatActivity() {

    private val loginBtn by lazy { findViewById<LoadingButton>(R.id.btn_enter) }
    private val roomNameEditText by lazy { findViewById<EditText>(R.id.tiet_room_name) }
    private val roomPwdEditText by lazy { findViewById<EditText>(R.id.tiet_room_pwd) }
    private val userNameEditText by lazy { findViewById<EditText>(R.id.tiet_name) }
    private val durationEditText by lazy { findViewById<EditText>(R.id.tiet_duration) }
    private val maxPeopleEditText by lazy { findViewById<EditText>(R.id.tiet_max_people) }
    private val inOutLimitEditText by lazy { findViewById<EditText>(R.id.tiet_in_out_limit_count) }
    private val cameraSwitch by lazy { findViewById<Switch>(R.id.sw_camera) }
    private val micSwitch by lazy { findViewById<Switch>(R.id.sw_mic) }
    private val screenSwitch by lazy { findViewById<Switch>(R.id.sw_screen) }
    private val meetingSDK by lazy { MainApplication.getMeetingSDK(this) }


    private val onExitListener = object: OnExitListener {
        override fun onExit(saved: RoomCache, reason: ExistReason) {
            Log.d("LoginActivity", "onExitListener onExit saved=$saved")
            ToastUtil.showShort("Meeting Exit")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginBtn.setOnClickListener { launchMeetingSdk() }

        screenSwitch.isChecked = requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        screenSwitch.setOnCheckedChangeListener{ _: CompoundButton, checked: Boolean ->
            if(checked){
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }else{
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }

        meetingSDK.setOnExitListener(onExitListener)
    }

    private fun launchMeetingSdk() {
        loginBtn.showLoading()
        val userId = CryptoUtil.md5(userNameEditText.text.toString())!!

        val roomId = CryptoUtil.md5(roomNameEditText.text.toString())!!
        meetingSDK.launch(
                LaunchConfig(
                        roomId,
                        roomNameEditText.text.toString(),
                        roomPwdEditText.text.toString(),
                        userId,
                        userNameEditText.text.toString(),
                        RtmTokenBuilder().buildToken(getString(R.string.agora_app_id), getString(R.string.agora_app_cert), userId, RtmTokenBuilder.Role.Rtm_User, 0),
                        durationEditText.text.toString().toLong(),
                        maxPeopleEditText.text.toString().toInt(),
                        cameraSwitch.isChecked,
                        micSwitch.isChecked,
                        CameraDirection.Front,
                        inOutLimitEditText.text.toString().toInt(),
                        mapOf(Pair("roomId", roomId), Pair("userId", userId))
                ),
                {
                    loginBtn.showButtonText()
                },
                {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                    loginBtn.showButtonText()
                },
                MainActivity::class.java
        )
    }

}